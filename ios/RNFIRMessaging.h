
#import <UIKit/UIKit.h>

@import FirebaseInstanceID;

#import <React/RCTBridgeModule.h>


extern NSString *const FCMNotificationReceived;

@interface RNFIRMessaging : NSObject <RCTBridgeModule>
+(void)setLastUserInfo:(NSDictionary *) data;
+(void)clearLastUserInfo;
@property (nonatomic, assign) bool connectedToFCM;

@end
