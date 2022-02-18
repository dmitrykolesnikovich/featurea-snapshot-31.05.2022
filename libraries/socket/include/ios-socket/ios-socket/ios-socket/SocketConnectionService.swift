import Foundation
import Socket

@objc public protocol SocketConnection {
    @objc func onThreadStartSocketConnection()
    @objc func onResponse(count: Int, response: Data)
    @objc func onReconnectSuccessSocketConnection(error: Error)
    @objc func onReconnectFailedSocketConnection(error: Error)
    @objc func onConnectSuccessSocketConnection()
    @objc func onConnectFailedSocketConnection(error: Error)
    @objc func onDisconnectSocketConnection()
    @objc func onResetSocket()
}

// https://www.avanderlee.com/swift/concurrent-serial-dispatchqueue
@objc public class SocketConnectionService : NSObject {
    
    private let socketConnection: SocketConnection
    private let ip: NSString
    private let port: NSNumber
    private let responseTimeout: NSNumber
    private let retriesCount: NSNumber
    private let retryTimeout: NSNumber
    
    private var data = Data()
    private var socket: Socket? = nil
    private var isDestroyed: Bool = false
    private var isCreated: Bool = false
    private var hasDisconnect: Bool = false
    private var connectRequestCount: AtomicInteger<Int> = AtomicInteger<Int>(0)
    private var currentRetriesCount: Int = 0
    private var hasRetries: Bool = true

    private let lockQueue = DispatchQueue(label: "lockQueue", attributes: .concurrent)
    private let lockObject = DispatchSemaphore(value: 0)
    private let syncQueue = DispatchQueue(label: "syncQueue")
    
    @objc public init(socketConnection: SocketConnection, ip: NSString, port: NSNumber, responseTimeout: NSNumber, retriesCount: NSNumber, retryTimeout: NSNumber) {
        self.socketConnection = socketConnection
        self.ip = ip
        self.port = port
        self.responseTimeout = responseTimeout
        self.retriesCount = retriesCount
        self.retryTimeout = retryTimeout
        print("ip: \(ip), port: \(port), responseTimeout: \(responseTimeout), retriesCount: \(retriesCount), retryTimeout: \(retryTimeout)")
    }
    
    @objc public func start() {
        print("SocketConnectionService.start #0")
        syncQueue.sync {
            print("SocketConnectionService.start #1")
            isCreated = true
            socketConnection.onThreadStartSocketConnection()
            print("SocketConnectionService.start #2")
            lockQueue.async {
                print("SocketConnectionService.start #3")
                self.run()
                print("SocketConnectionService.start #4")
            }
            print("SocketConnectionService.start #5")
        }
        print("SocketConnectionService.start #6")
    }
    
    private func run() {
        print("SocketConnectionService.run #0")
        while (!isDestroyed) {
            print("SocketConnectionService.run #1")
            if (connectRequestCount.get() > 0) {
                print("SocketConnectionService.run #2")
                connectRequestCount -= 1
                print("SocketConnectionService.run #3")
                newSocket()
                print("SocketConnectionService.run #4")
            } else if (isConnected()) {
                print("SocketConnectionService.run #5")
                read()
                print("SocketConnectionService.run #6")
            } else {
                print("SocketConnectionService.run #7")
                while (shouldWait()) {
                    print("SocketConnectionService.run #8")
                    lockObject.wait()
                    print("SocketConnectionService.run #9")
                }
                print("SocketConnectionService.run #10")
            }
            print("SocketConnectionService.run #11")
        }
        print("SocketConnectionService.run #12")
    }
    
    private func shouldWait() -> Bool {
        syncQueue.sync {
            let shouldWait = !isDestroyed && connectRequestCount.get() <= 0
            if (shouldWait) {
                connectRequestCount.set(value: 0)
            }
            return shouldWait
        }
    }
    
    private func read() {
        print("SocketConnectionService.read #0")
        if (!isConnected()) {
            return;
        }
        print("SocketConnectionService.read #1")
        do {
            data.count = 0
            print("SocketConnectionService.read #2")
            let bytesRead = try socket!.read(into: &data)
            print("SocketConnectionService.read #3")
            if bytesRead > 0 {
                print("SocketConnectionService.read #4: \(bytesRead)")
                var responseData = Data(data)
                responseData.count = data.count
                runOnMain(block: {
                    self.socketConnection.onResponse(count: bytesRead, response: responseData)
                })
                print("SocketConnectionService.read #5")
            }
            print("SocketConnectionService.read #6")
        } catch let error {
            print("SocketConnectionService.read #7")
            reconnect(socket: socket, error: error);
            print("SocketConnectionService.read #8")
        }
        print("SocketConnectionService.read #9")
    }
    
    private func reconnect(socket: Socket?, error: Error) {
        print("SocketConnectionService.reconnect #0")
        syncQueue.sync {
            print("SocketConnectionService.reconnect #1")
            if (socket != nil && socket != self.socket) {
                return;
            }
            print("SocketConnectionService.reconnect #2")
            if (!hasDisconnect) {
                print("SocketConnectionService.reconnect #3")
                socketConnection.onReconnectSuccessSocketConnection(error: error);
                print("SocketConnectionService.reconnect #4")
                connect();
                print("SocketConnectionService.reconnect #5")
            } else {
                print("SocketConnectionService.reconnect #6")
                socketConnection.onReconnectFailedSocketConnection(error: error);
                print("SocketConnectionService.reconnect #7")
            }
            print("SocketConnectionService.reconnect #8")
        }
        print("SocketConnectionService.reconnect #9")
    }
    
    private func newSocket() {
        print("SocketConnectionService.newSocket #0")
        let startTime = currentTimeInMilliSeconds()
        do {
            print("SocketConnectionService.newSocket #1")
            let socket = try Socket.create(family: .inet)
            print("SocketConnectionService.newSocket #2: \(ip), \(port)")
            try socket.connect(to: ip as String, port: Int32(truncating: port), timeout: UInt(truncating: responseTimeout))
            print("SocketConnectionService.newSocket #3")
            setSocket(socket: socket);
            print("SocketConnectionService.newSocket #4")
            runOnMain(block: {
                self.socketConnection.onConnectSuccessSocketConnection()
            })
            print("SocketConnectionService.newSocket #5")
        } catch let error {
            print("SocketConnectionService.newSocket #6")
            let finishTime = currentTimeInMilliSeconds()
            let deltaTime = finishTime - startTime;
            let sleepTime = retryTimeout.doubleValue - deltaTime;
            print("SocketConnectionService.newSocket #7")
            if (sleepTime > 0) {
                print("SocketConnectionService.newSocket #8")
                sleep(UInt32(sleepTime));
                print("SocketConnectionService.newSocket #9")
            }
            print("SocketConnectionService.newSocket #10")
            socketConnection.onConnectFailedSocketConnection(error: error);
            print("SocketConnectionService.newSocket #11")
            reconnect(socket: socket, error: error);
            print("SocketConnectionService.newSocket #12")
        }
        print("SocketConnectionService.newSocket #13")
    }
    
    @objc public func connect() {
        print("SocketConnectionService.connect #0")
        hasDisconnect = false; // by design
        print("SocketConnectionService.connect #1")
        setSocket(socket: nil);
        print("SocketConnectionService.connect #2")
        currentRetriesCount += 1
        print("SocketConnectionService.connect #3: \(currentRetriesCount), \(retriesCount.int32Value)")
        if (currentRetriesCount > retriesCount.int32Value) {
            print("SocketConnectionService.connect #4")
            hasRetries = false;
            print("SocketConnectionService.connect #5")
            return;
        }
        print("SocketConnectionService.connect #6")
        syncQueue.sync {
            print("SocketConnectionService.connect #7")
            connectRequestCount += 1
            print("SocketConnectionService.connect #8")
            notify()
            print("SocketConnectionService.connect #9")
        }
        print("SocketConnectionService.connect #10")
    }
    
    @objc public func destroy() {
        print("SocketConnectionService.destroy #0")
        syncQueue.sync {
            print("SocketConnectionService.destroy #1")
            if (isDestroyed) {
                return
            }
            print("SocketConnectionService.destroy #2")
            self.isDestroyed = true
            print("SocketConnectionService.destroy #3")
            notify()
            print("SocketConnectionService.destroy #4")
        }
        print("SocketConnectionService.destroy #5")
    }
    
    @objc public func write(bytes: Data) -> Bool {
        print("SocketConnectionService.write #0")
        guard let socket = socket else { return false }
        print("SocketConnectionService.write #1")
        if (!isConnected()) {
            return false;
        }
        print("SocketConnectionService.write #2")
        do {
            print("SocketConnectionService.write #3")
            try socket.write(from: bytes)
            print("SocketConnectionService.write #4")
            return true;
        } catch let error {
            print("SocketConnectionService.write #5")
            reconnect(socket: socket, error: error);
            return false;
        }
        print("SocketConnectionService.write #6")
    }
    
    @objc public func isConnected() -> Bool {
        print("SocketConnectionService.isConnected #0")
        guard let socket = socket else { return false }
        print("SocketConnectionService.isConnected #1")
        let isConnected = socket.isConnected && socket.isActive;
        print("SocketConnectionService.isConnected #2")
        if (!isConnected) {
            print("SocketConnectionService.isConnected #3")
        }
        return isConnected;
    }
    
    private func setSocket(socket: Socket?) {
        print("SocketConnectionService.setSocket #0")
        syncQueue.sync {
            print("SocketConnectionService.setSocket #1")
            if (self.socket != nil) {
                self.socket?.close();
                self.socket = nil;
                socketConnection.onDisconnectSocketConnection()
            }
            print("SocketConnectionService.setSocket #2")
            self.socket = socket;
            if (self.socket != nil) {
                currentRetriesCount = 0;
            }
            print("SocketConnectionService.setSocket #3")
            runOnMain(block: {
                print("SocketConnectionService.setSocket #4")
                self.socketConnection.onResetSocket()
                print("SocketConnectionService.setSocket #5")
            })
            print("SocketConnectionService.setSocket #6")
        }
    }
    
    @objc public func disconnect() {
        print("SocketConnectionService.disconnect #0")
        hasDisconnect = true
        print("SocketConnectionService.disconnect #1")
        setSocket(socket: nil)
        print("SocketConnectionService.disconnect #2")
    }
    
    private func notify() {
        print("SocketConnectionService.notify #0")
        lockQueue.async {
            print("SocketConnectionService.notify #1")
            self.lockObject.signal()
            print("SocketConnectionService.notify #2")
        }
        print("SocketConnectionService.notify #3")
    }
    
    func runOnMain(block: @escaping () -> Void) {
        print("SocketConnectionService.runOnMain #0")
        DispatchQueue.global().async(execute: {
            print("SocketConnectionService.runOnMain #1")
            DispatchQueue.main.sync {
                print("SocketConnectionService.runOnMain #2")
                block()
                print("SocketConnectionService.runOnMain #3")
            }
            print("SocketConnectionService.runOnMain #4")
        })
        print("SocketConnectionService.runOnMain #5")
    }
    
}

private func currentTimeInMilliSeconds() -> Double {
    let currentDate = Date()
    let since1970 = currentDate.timeIntervalSince1970
    return Double(since1970 * 1000)
}
