import Foundation

class Message {
    
    private var bytes: [UInt8]
    
    private var headerSize: Int = -1
    private var headerStartIndex = -1
    private var headerFinishIndex = -1
    
    private var fileNameSize: Int = -1
    private var fileNameStartIndex = -1
    private var fileNameFinishIndex = -1
    
    init(bytes: [UInt8]) {
        self.bytes = bytes
    }
    
    func readHeader() -> NSString? {
        let offset = 0
        headerSize = Int(UInt(bytes.readShort(index: offset)))
        headerStartIndex = offset + 2
        headerFinishIndex = headerStartIndex + headerSize - 1
        return NSString(data: Data(bytes[headerStartIndex...headerFinishIndex]), encoding: String.Encoding.utf8.rawValue)
    }
    
    func readFileName() -> NSString? {
        let offset = headerFinishIndex + 1
        fileNameSize = Int(UInt(bytes.readShort(index: offset)))
        fileNameStartIndex = offset + 2
        fileNameFinishIndex = fileNameStartIndex + fileNameSize - 1
        return NSString(data: Data(bytes[fileNameStartIndex...fileNameFinishIndex]), encoding: String.Encoding.utf8.rawValue)
    }
    
    func getSize() -> Int {
        return fileNameFinishIndex + 1
    }
}
