//
//  QuestionViewController.swift
//  quintessence-learning
//
//  Created by Eric Feng on 7/24/17.
//  Copyright © 2017 Eric Feng. All rights reserved.
//

import UIKit
import UserNotifications
import FirebaseAuth
import FirebaseDatabase
class QuestionViewController: UIViewController {

    var notifyTime:Date?
    var timer:Timer?
    var dayInSeconds:Double = 60
    var notificationContent = UNMutableNotificationContent()
    var user:User?
    var ref:DatabaseReference?
    var currentQuestionKey = ""
    
//    var dayInSeconds:Double = 86400
    
    @IBOutlet weak var questionLabel: UITextView!
    @IBOutlet weak var savedLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //grab variables from tab controller
        
        let tabController = self.tabBarController as! UserTabBarController
        notifyTime = tabController.notifyTime!
        
        user = Auth.auth().currentUser!
        ref = Database.database().reference()
        if (tabController.isFirstTime!){
            setQuestionTimer()
        }
        getQuestion()
        self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: "Logout", style: .plain, target: self, action: #selector(showLogout))
        self.navigationItem.rightBarButtonItem = UIBarButtonItem(barButtonSystemItem: .organize, target: self, action: #selector(showSaveOptions))
    }
    
    //begins the question timer
    func setQuestionTimer(){
        if let timer = timer {
            timer.invalidate()
        }
        //if the time is later, set it for that time the next day
        //ex. if it is 5 PM and user sets 8 PM, want to fire both notif/question change the next day at 8PM, not 3 hours later
        if (isNotifyTimeLater()){
            print("it's later")
            notifyTime!.addTimeInterval(TimeInterval(dayInSeconds))
            
            //set fire setNotificationTimer in 24 hours
            let nextDay = Date().addingTimeInterval(dayInSeconds)
            let notifTimer = Timer(fireAt: nextDay, interval: 0, target: self, selector: #selector(setNotificationTimer), userInfo: nil, repeats: false)
            RunLoop.main.add(notifTimer, forMode: RunLoopMode.commonModes)
        } else {
            setNotificationTimer()
        }
        timer = Timer(fireAt: notifyTime!, interval: dayInSeconds, target: self, selector: #selector(getNextQuestion), userInfo: nil, repeats: true)
        RunLoop.main.add(timer!, forMode: RunLoopMode.commonModes)
        print("timer set!")
        
        let tabController = self.tabBarController as! UserTabBarController
        tabController.isFirstTime = false
    }
    
    //sets the notification timer
    func setNotificationTimer(){
        var dateComponents = DateComponents()
        let components = getHourAndMinutes(date: notifyTime!)
        dateComponents.hour = components[0]
        dateComponents.minute = components[1]
        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
//        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 10, repeats: false)
        
        notificationContent.title = "New Question Today!"
        notificationContent.body = "Swipe to open question..."
        notificationContent.categoryIdentifier = "alarm"
        notificationContent.userInfo = ["wtf":"???"]
        notificationContent.sound = UNNotificationSound.default()
        
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: notificationContent, trigger: trigger)
        UNUserNotificationCenter.current().add(request)
        print("notification timer set!")
    }
    
    //determines if the time selected to notify is later than current, and if so, 24 hr delay must be implemented
    func isNotifyTimeLater() -> Bool {
        let notify = getHourAndMinutes(date: notifyTime!)
        let current = getHourAndMinutes(date: Date())
        if (notify[0] < current[0]){
            return false
        }
        if (notify[0] == current[0]) {
            if (notify[1] < current[1]) {
                return false
            }
            else {
                return true
            }
        }
        return true
    }
    
    //helper method for isNotifyTimeLater
    func getHourAndMinutes(date:Date) -> [Int] {
        let calendar = Calendar.current
        let hour = calendar.component(.hour, from: date)
        let minutes = calendar.component(.minute, from: date)
        return [hour, minutes]
    }
    
    //gets the next question by incrementing this user's count
    func getNextQuestion() {
        print("getting next question")
        self.ref!.child("Users").child(user!.uid).child("Current_Question").observeSingleEvent(of: .value, with: { (snapshot) in
            let value = snapshot.value as! Int
            self.ref!.child("Users").child(self.user!.uid).child("Current_Question").setValue(value+1)
            self.getQuestion()
        })
    }

    //retrieves a question according to this user's count
    func getQuestion(){
        print("getting question")
        //get the count
        self.ref!.child("Users").child(user!.uid).child("Current_Question").observeSingleEvent(of: .value, with: { (snapshot) in
            let value = snapshot.value as! Int
            //get the first question greater than or equal to count
            self.ref!.child("Questions").queryOrdered(byChild: "count").queryStarting(atValue: value).queryLimited(toFirst: 1).observeSingleEvent(of: .value, with: {(snapshot) in
                let result = snapshot.children.allObjects as! [DataSnapshot]
                if (result.count == 0) {
                    self.questionLabel.text = "Unable to load question"
                }
                let qbody = result[0].value as? NSDictionary
                if let qbody = qbody {
                    self.questionLabel.text = qbody["Text"] as? String ?? "Unable to load question"
                    self.currentQuestionKey = qbody["Key"] as? String ?? ""
                    
                } else {
                    self.questionLabel.text = "Unable to load question"
                }
            })
        }) { (err) in
            print(err)
        }
    }
 
    //shows the save actionSheet
    func showSaveOptions(){
        let ac = UIAlertController(title: "Question Options", message: nil, preferredStyle: .actionSheet)
        ac.addAction(UIAlertAction(title: "Save this question", style: .default, handler: { (action:UIAlertAction) in
            self.ref!.child("Users").child(self.user!.uid).child("Saved").childByAutoId().setValue(self.currentQuestionKey)
            
        }))
        ac.addAction(UIAlertAction(title: "View all saved questions", style: .default, handler: { (action:UIAlertAction ) in
            let savedView = self.storyboard?.instantiateViewController(withIdentifier: "SavedQ") as! SavedTableViewController
            self.present(savedView, animated: true)
        }))
        ac.addAction(UIAlertAction(title: "Cancel", style: .destructive))
        present(ac, animated: true)
    }
    
    
    func showLogout(){
        let ac = UIAlertController(title: "Confirm", message: "Are you sure you want to log out?", preferredStyle: .alert)
        ac.addAction(UIAlertAction(title: "Logout", style: .default, handler: logout(action: )))
        ac.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        present(ac, animated: true)
    }
    
    func logout(action:UIAlertAction){
        do {
            try Auth.auth().signOut()
            let welcomeScreen = self.storyboard?.instantiateViewController(withIdentifier: "Home") as! UINavigationController
            self.navigationController!.present(welcomeScreen, animated: true)
        } catch {
            Server.showError(message: error.localizedDescription)
        }
    }
}
