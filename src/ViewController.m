#import "ViewController.h"

@interface ViewController () <WKScriptMessageHandler>

@property (weak, nonatomic) IBOutlet WKWebView *root;

@end

@implementation ViewController


- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    /*    [_root loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"https://3o3.org"]]];
     */
    WKWebViewConfiguration *config =[_root configuration];
     [[config userContentController] addScriptMessageHandler:self name: @"log"];

    [_root evaluateJavaScript:@"document.body.innerHTML = '<h1>jml</h1>';window.webkit.messageHandlers.log.postMessage('yo');" completionHandler:^(id result, NSError *error) {NSLog([NSString stringWithFormat:@"%@", result]);}];
    
    
}
- (void)userContentController:(WKUserContentController *)userContentController
       didReceiveScriptMessage:(WKScriptMessage *)message {
     if ([message.name isEqualToString:@"log"])
         NSLog(@"%@", message.body);

 }
@end
