import Foundation

public class FileWriter {
    
    let dir: NSURL
    
    public init(dir: NSURL) {
        self.dir = dir
    }
    
    private var fileName: NSString?
    
    func createNewFile(fileName: NSString, bytes: [UInt8], offset: Int) throws -> URL  {
        print("createNewFile #0")
        self.fileName = fileName
        print("createNewFile #1")
        let fileUrl = toUrl(fileName: fileName)
        print("createNewFile #2")
        if (FileManager.default.fileExists(atPath: fileUrl.path)) {
            print("createNewFile #3")
            try FileManager.default.removeItem(at: fileUrl)
            print("createNewFile #4")
        }
        print("createNewFile #6")
        try Data(bytes[offset...bytes.count - 1]).write(to: fileUrl, options: .atomic)
        print("createNewFile #7")
        return fileUrl
    }
    
    func saveToFile(bytes: [UInt8]) throws {
        let fileUrl = toUrl(fileName: fileName!)
        if let fileHandle = try? FileHandle(forWritingTo: fileUrl) {
            fileHandle.seekToEndOfFile()
            fileHandle.write(Data(bytes))
            fileHandle.closeFile()
        }
    }
    
    func toUrl(fileName: NSString) -> URL {
        return URL(fileURLWithPath: dir.appendingPathComponent(fileName as String)!.path)
    }
    
}
