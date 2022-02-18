import Foundation

@objc public class FileWatcher : NSObject {
    
    private var isStart: Bool = false
    private let url: URL
    private var monitorDescriptor: CInt = -1
    private var folderMonitorSource: DispatchSourceFileSystemObject!
    
    @objc public var folderDidChange: (() -> Void)!
    
    @objc public init(url: URL) {        
        self.url = url
        super.init()
    }
    
    @objc public func start() {
        if (isStart) { return }
        isStart = true
        monitorDescriptor = open(url.path, O_EVTONLY)
        folderMonitorSource = DispatchSource.makeFileSystemObjectSource(fileDescriptor: monitorDescriptor, eventMask: .all, queue: DispatchQueue(label: "FolderMonitorQueue", attributes: .concurrent))
        folderMonitorSource.setEventHandler {
            DispatchQueue.main.async {
                self.folderDidChange()
            }            
        }
        folderMonitorSource.setCancelHandler {
            close(self.monitorDescriptor)
            self.monitorDescriptor = -1
            self.folderMonitorSource = nil
        }
        folderMonitorSource.resume()
    }
    
    @objc public func stop() {
        if (!isStart) { return }
        folderMonitorSource.cancel()
        isStart = false
    }
    
}
