import Foundation
import Socket
import ZIPService

@objc public class FileTransfer : NSObject {
    
    let multicastIp = "230.0.0.1";
    let multicastPort = Int32(4444);
    var serverSocket: Socket?
    var udpSocket: Socket?
    private let fileWriter: FileWriter!
    
    @objc public init(dir: NSURL) {
        self.fileWriter = FileWriter(dir: dir)
    }
    
    @objc public func create() {
        let globalQueue = DispatchQueue.global(qos: .utility)
        globalQueue.async {
            do {
                // upload file server
                self.serverSocket = try Socket.create(family: Socket.ProtocolFamily.inet)
                try self.serverSocket?.listen(on: 0, maxBacklogSize: 10)
                print("Listening on port: \(self.serverSocket!.listeningPort)")
                
                self.connectFtpInfoProducer()
                
                self.readClientSocket()
            } catch let error {
                guard let socketError = error as? Socket.Error else { return }
                if socketError.errorCode == Int32(Socket.SOCKET_ERR_WRITE_FAILED) { return }
                print("serverHelper Error reported: \(socketError.description)")
            }
        }
    }
    
    private func connectFtpInfoProducer() {
        let PAYLOAD_DELIMITER = "\n";
        let MESSAGE_PREFIX = "\n\n";
        let FTP_INFO_HEADER = "FTP_INFO_HEADER";
        let host = getWiFiAddress() ?? "null"
        print("host: \(host)")
        let payload = "\(host)\(PAYLOAD_DELIMITER)\(serverSocket!.listeningPort)\(PAYLOAD_DELIMITER)Apple\(PAYLOAD_DELIMITER)iPhone 5s"
        let message = "\(FTP_INFO_HEADER)\(MESSAGE_PREFIX)\(payload)"
        DispatchQueue.main.async {
            do {
                self.udpSocket = try Socket.create(family: Socket.ProtocolFamily.inet, type: .datagram, proto: .udp)
                let address = Socket.createAddress(for: self.multicastIp, on: self.multicastPort)
                let timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true, block: { timer in
                    do {
                        try self.udpSocket?.write(from: message.data(using: .utf8)!, to: address!)
                    } catch let error {
                        print("error: \(error.localizedDescription)")
                    }
                })
                RunLoop.current.add(timer, forMode: RunLoop.Mode.common)
            } catch let error {
                print("error: \(error.localizedDescription)")
            }
        }
    }
    
    
    @objc public func destroy() {
        serverSocket?.close()
        udpSocket?.close()
    }
    
    private func readClientSocket() {
        do {
            repeat {
                let clientSocket = try serverSocket?.acceptClientConnection()
                // read clientSocket
                /*print("Accepted connection from: \(clientSocket!.remoteHostname) on port \(clientSocket!.remotePort), Secure? \(clientSocket!.signature!.isSecure)")*/
                var bytesRead = 0
                var messageType: NSString? = nil
                var fileUrl: URL? = nil
                repeat {
                    var readData = Data()
                    bytesRead = try clientSocket!.read(into: &readData)
                    if bytesRead > 0 {
                        if (messageType == nil) {
                            let message = Message(bytes: readData.bytes)
                            guard let header: NSString = message.readHeader() else { return }
                            /*print("header: \(header) ***")*/
                            switch(header) {
                            case "UPLOAD_FILE_HEADER":
                                messageType = header
                                let fileName = message.readFileName()
                                print("1")
                                fileUrl = try fileWriter.createNewFile(fileName: fileName!, bytes: readData.bytes, offset: message.getSize())
                                print("2")
                                break
                            default:
                                print()
                                break
                            }
                        } else {
                            print("3")
                            try fileWriter.saveToFile(bytes: readData.bytes)
                            print("4")
                        }
                    }
                    if bytesRead == 0 {
                        if (messageType == "UPLOAD_FILE_HEADER") {
                            unzipBundle(sourceURL: fileUrl!)
                        }
                        messageType = nil
                        fileUrl = nil
                        break
                    }
                } while true
                clientSocket!.close()
            } while true
        } catch let error as NSError {
             print("readClientSocket error: \(error.description)")
         }
    }
    
    private func disconnect() {
        // no op
    }
    
    private func unzipBundle(sourceURL: URL) {
        let string1 = sourceURL.path
        print("string1: \(string1)")
        let string2 = string1.replacingOccurrences(of: ".bundle", with: "_bundle")
        print("string2: \(string2)")
        let destinationURL = URL(fileURLWithPath: string2)
        do {
            print("unzipBundle #0")
            try FileManager.default.createDirectory(at: destinationURL, withIntermediateDirectories: true, attributes: nil)
            print("unzipBundle #1: \(sourceURL.path), \(destinationURL.path)")
            try FileManager.default.unzipItem(at: sourceURL, to: destinationURL)
            print("unzipBundle #2")
        } catch {
            print("Extraction of ZIP archive failed with error:\(error)")
        }
    }
    
}
