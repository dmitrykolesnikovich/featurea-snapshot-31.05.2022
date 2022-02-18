import Foundation

@objc open class ModbusClientDelegate : NSObject {
    @objc public override init() {}
    @objc public var isDestroy: Bool = false
    @objc public var isConnect: Bool = false
    @objc public var onTick: ((Double) -> Void)? = nil
}

@objc public class ModbusThreadDelegate : NSObject {
    
    private var modbusClient: ModbusClientDelegate
    private var queue = DispatchQueue(label: "ModbusThread", attributes: .concurrent)
    private let semaphore = DispatchSemaphore(value: 0)
    private var now: Double = 0.0
        
    @objc public init(modbusClient: ModbusClientDelegate) {
        self.modbusClient = modbusClient
    }
    
    @objc public func start() {
        queue.async {
            let startTime = NSDate().timeIntervalSince1970 * 1000
            if (self.now == 0.0) {
                self.now = startTime
            }
            
            while(!self.modbusClient.isDestroy) {
                let startTime = NSDate().timeIntervalSince1970 * 1000
                if (self.now == 0.0) {
                    self.now = startTime
                }
                let elapsedTime = startTime - self.now
                self.now = startTime
                if (self.modbusClient.isConnect) {
                    self.modbusClient.onTick?(elapsedTime)
                    Thread.sleep(forTimeInterval: 0.3)
                } else {
                    do {
                        while (!self.modbusClient.isConnect && !self.modbusClient.isDestroy) {
                            self.semaphore.wait()
                        }
                    } catch let error {
                        print("error: \(error.localizedDescription)")
                    }
                    self.now = NSDate().timeIntervalSince1970 * 1000
                }
            }
        }
    }
    
    @objc public func onStateChange() {
        queue.async {
            self.semaphore.signal()
        }
    }
    
}
