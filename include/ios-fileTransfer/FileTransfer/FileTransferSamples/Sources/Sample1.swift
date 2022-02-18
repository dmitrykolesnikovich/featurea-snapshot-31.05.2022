import Foundation
import UIKit
import Socket
import Dispatch
import UIKit
import FileTransfer

class Sample1 : UIViewController {
    
    private let startButton = UIButton.init(type: .system)
    private let stopButton = UIButton.init(type: .system)
    private let fileTransfer = FileTransfer(dir: NSURL(string: FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!.path)!)
    
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
        
        // stopButton
        stopButton.setTitle("Stop", for: UIControl.State.normal)
        view.addSubview(stopButton)
        stopButton.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
          stopButton.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 10),
          stopButton.topAnchor.constraint(equalTo: startButton.bottomAnchor),
          stopButton.heightAnchor.constraint(equalToConstant: 48)
        ])
        
        startButton.addTarget(self, action: #selector(didButtonClick), for: .touchUpInside)
        stopButton.addTarget(self, action: #selector(didButtonClick), for: .touchUpInside)
    }
    
    @objc func didButtonClick(_ sender: AnyObject?) {
        if sender === startButton {
            fileTransfer.create()
        } else if sender === stopButton {
            fileTransfer.destroy()
        }
        
    }
    
}
