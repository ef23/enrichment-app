//
//  ViewController.swift
//  quintessence-learning
//
//  Created by Eric Feng on 7/18/17.
//  Copyright © 2017 Eric Feng. All rights reserved.
//

import UIKit
import StoreKit
import FirebaseAuth
import FirebaseDatabase
class ViewController: UIViewController {

    var ref : Database?
    var didViewChange = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        ref = Database.database()
        if (Reachability.isConnectedToNetwork()){
            //redirects a logged in user to the appropriate view
            Auth.auth().addStateDidChangeListener() { auth, user in
                if user != nil {
                    self.ref!.reference().child(Common.USER_PATH).child(user!.uid).observe(.value, with: { (snapshot) in
                        let value = snapshot.value as? NSDictionary
                        let userType = value?["Type"] as? String ?? ""
                        if (userType != "none") {
                            if (userType == "basic") {
                                Common.timeInterval = Common.weekInSeconds
                            }
                            else if (userType == "trial") {
                                //check if trial is expired
                                let joinDateSinceEpoch = value?["Join_Date"] as! TimeInterval
                                
                                //Firebase uses milliseconds while Swift uses seconds, need to do conversion
                                //calculate number of days left in trial
                                let timeElapsed = Double(Common.trialLength) * Common.dayInSeconds - (Date().timeIntervalSince1970 - joinDateSinceEpoch/1000)
                                if (timeElapsed <= 0){
                                    self.showPremiumScreen()
                                } else {
                                    let userViewController = self.storyboard?.instantiateViewController(withIdentifier: "User") as! UITabBarController
                                    self.present(userViewController, animated: true)
                                }

                            } else {
                                //check if premium subscription is active
                                guard SubscriptionService.shared.currentSessionId != nil,
                                    SubscriptionService.shared.hasReceiptData else {
                                        //show premium screen if not
                                        self.showPremiumScreen()
                                        return
                                }
                                Common.timeInterval = Common.dayInSeconds
                            }
                            let userViewController = self.storyboard?.instantiateViewController(withIdentifier: "User") as! UITabBarController
                            self.present(userViewController, animated: true)
                            return
                        } else {
                            self.showLoginScreen()
                            return
                        }
                    }) { (error) in
                        debugPrint("Failed get the snapshot \(error.localizedDescription)")
                    }
                }
                else {
                    self.showLoginScreen()
                }
            }
        } else {
            Server.showError(message: "No internet connection detected! Please connect to the internet and try again.")
            showLoginScreen()
        }
    }
    
    func showLoginScreen(){
        let welcomeScreen = self.storyboard?.instantiateViewController(withIdentifier: "Home") as! UINavigationController
        self.present(welcomeScreen, animated: true)
    }
    
    func showPremiumScreen() {
        let premiumScreen = self.storyboard?.instantiateViewController(withIdentifier: "Premium") as! PremiumPurchaseViewController
        self.present(premiumScreen, animated: true)
    }
    
}




extension UIViewController {
    func hideKeyboardOnTap() {
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(UIViewController.dismissKeyboard))
        tap.cancelsTouchesInView = false
        view.addGestureRecognizer(tap)
    }
    
    func dismissKeyboard() {
        view.endEditing(true)
    }
}
