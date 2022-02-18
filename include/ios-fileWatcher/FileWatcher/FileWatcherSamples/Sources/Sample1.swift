import Foundation
import UIKit
import FileWatcher

private let documentDir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!

class Sample1 : UIViewController {
    
    private let startButton = UIButton.init(type: .system)
    private let stopButton = UIButton.init(type: .system)
    private let fileWatcher = FileWatcher(url: documentDir)
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.white
        
        // startButton
        startButton.setTitle("Start", for: UIControl.State.normal)
        view.addSubview(startButton)
        startButton.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            startButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10),
            startButton.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor),
            startButton.heightAnchor.constraint(equalToConstant: 48)
        ])
        startButton.addTarget(self, action: #selector(didButtonClick), for: .touchUpInside)
        
        // stopButton
        stopButton.setTitle("Stop", for: UIControl.State.normal)
        view.addSubview(stopButton)
        stopButton.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            stopButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10),
            stopButton.topAnchor.constraint(equalTo: startButton.bottomAnchor),
            stopButton.heightAnchor.constraint(equalToConstant: 48)
        ])
        stopButton.addTarget(self, action: #selector(didButtonClick), for: .touchUpInside)
        
        // log results
        fileWatcher.folderDidChange = {
            print("--------------------- folderDidChange ---------------------")
            do {
                let fileUrls = try FileManager.default.contentsOfDirectory(at: documentDir, includingPropertiesForKeys: nil)
                for fileUrl in fileUrls {
                    print("\(fileUrl.path)\n")
                }
            } catch let error as NSError {
                print("\(error.description)")
            }
        }
    }
    
    @objc func didButtonClick(_ sender: AnyObject?) {
        if sender === startButton {
            fileWatcher.start()
        } else if sender === stopButton {
            fileWatcher.stop()
        }
    }
    
}
