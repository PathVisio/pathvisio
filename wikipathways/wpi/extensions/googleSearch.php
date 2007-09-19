<?php
# Google Custom Search Engine Extension
# 
# Tag :
#   <Googlecoop></Googlecoop>
# Ex :
#   Add this tag to the wiki page you configed at your google co-op control panel.
#   
# 
# Enjoy !

$wgExtensionFunctions[] = 'GoogleCoop';
$wgExtensionCredits['parserhook'][] = array(
        'name' => 'Google Co-op Extension',
        'description' => 'Using Google Co-op',
        'author' => 'Liang Chen The BiGreat',
        'url' => 'http://liang-chen.com'
);

function GoogleCoop() {
        global $wgParser;
        $wgParser->setHook('Googlecoop', 'renderGoogleCoop');
}

# The callback function for converting the input text to HTML output
function renderGoogleCoop($input) {
        
        $output='
<!-- Google Search Result Snippet Begins -->
  <div id="results_011541552088579423722:rset6ep3k64"></div>
  <script type="text/javascript">
    var googleSearchIframeName = "results_011541552088579423722:rset6ep3k64";
    var googleSearchFormName = "searchbox_011541552088579423722:rset6ep3k64";
    var googleSearchFrameWidth = 600;
    var googleSearchFrameborder = 0;
    var googleSearchDomain = "www.google.com";
    var googleSearchPath = "/cse";
  </script>
  <script type="text/javascript" src="http://www.google.com/afsonline/show_afs_search.js"></script>
<!-- Google Search Result Snippet Ends -->
<!-- Google Search Result Snippet Begins
  <div id="results_002915365922082279465:6qd0wwvwtwu"></div>
  <script type="text/javascript">
    var googleSearchIframeName = "results_002915365922082279465:6qd0wwvwtwu";
    var googleSearchFormName = "searchbox_002915365922082279465:6qd0wwvwtwu";
    var googleSearchFrameWidth = 600;
    var googleSearchFrameborder = 0;
    var googleSearchDomain = "www.google.com";
    var googleSearchPath = "/cse";	 
  </script>
  <script type="text/javascript" src="http://www.google.com/afsonline/show_afs_search.js"></script>
Google Search Result Snippet Ends -->
                                        ';//google code end here

        return $output;
}
?>
