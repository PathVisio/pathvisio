#!/usr/bin/perl
# mwup.pl - MediaWiki file UPLOAD script
#   by Kernigh - xkernigh AT netscape DOT net
#   version 2006-05-01 - this script is in the public domain
#
# Derived from:
# Upload script by Erik Möller - moeller AT scireview DOT de - public domain
# Developed for the Wikimedia Commons
#
# Note: Before usage, create an account on the destination MediaWiki
# for the bot. On Wikimedia Commons, the convention is
# "File Upload Bot (Username)", for example, File Upload Bot (Kernigh).
#
# Set the username and password below:

$username = "USERNAME";
$password = "PASSWORD";

# Set the pause in seconds after each upload
$pause = 120;

# List the wiki PHP scripts where you have the username/password pair
%wiki_php = (
  'wikipathways',           'http://blog.bigcat.unimaas.nl/pathwaywiki/index.php',
);

# Then run the script on the command line using
#
# $ perl mwup.pl wiki dirname
#
# where wiki is one of the wikis from the wiki_php list above,
# and dirname/ is the name of a directory containing the files to
# be uploaded, and a file named files.txt in the following format
#
# What you write                Explanation
#----------------------------------------------------------------------------
# @{{GFDL}} [[Category:Dog]]    This text is appended to every description.
# °Dog photo by Eloquence       This text is used when no description exists.
# >Dog01.jpg                    Name of a file in the specified directory.
# German shepherd dog           Description (can be multi-line).
# >Dog02.jpg                    File without a description (use default)
#
# The "@" and "°" lines are optional, and must be in one line. They can
# occur multiple times in a single file and are only valid until they
# are changed. As a consequence, description lines cannot start with "@"
# or "°".
#
# Don't edit below unless you know what you're doing.

# We need these libraries. They should be part of a standard Perl
# distribution.
use LWP::Simple;
use LWP::UserAgent;
use HTTP::Request;
use HTTP::Response;
use HTTP::Cookies;
use Encode qw(encode);
use warnings;

$ignore_login_error=0;
$docstring="Please read mwup.pl for documentation.\n";
my $wiki=$ARGV[0] or die  "Syntax: perl mwup.pl wiki directory\n$docstring";
my $dir=$ARGV[1] or die "Syntax: perl mwup.pl wiki directory\n$docstring";

# Find the wiki PHP script
$cgi = $wiki_php{$wiki} or die "Unknown wiki: $wiki\n$docstring";

# Make Unix style path
$dir=~s|\\|/|gi;

# Remove trailing slashes
$sep=$/; $/="/"; chomp($dir); $/=$sep;

# Now try to get the list of files
open(FILELIST,"<$dir/files.txt")
  or die "Could not find file list at $dir/files.txt.\n$docstring";


$standard_text[0]="";
$default_text[0]="";
$stx=0; $dtx=0;
while(<FILELIST>) {
        $line=$_;
        chomp($line);
        if($line=~m/^@/) {
                $line=~s/^@//;
                $standard_text[$stx]=$line;
                $stx++;
                $stw=1;
        }
        elsif($line=~m/^°/) {
                $line=~s/^°//;
                $default_text[$dtx]=$line;
                $dtx++;
                $dtw=1;
        }
        elsif($line=~m/^>/) {
                $line=~s/^>//;

                # New file, but last one doesn't have a description yet -
                # add current default.
                if($currentfile) {
                        # If there's been a change of the default or standard
                        # text, we need to apply the old text to the previous
                        # file, not the new one.
                        $dx= $dtw? $dtx-2 : $dtx -1;
                        $sx= $stw? $stx-2 : $stx -1;
                        if(!$desc_added) {
                                $file{$currentfile}.="\n".$default_text[$dx];
                        }
                        $file{$currentfile}.="\n\n".$standard_text[$sx];
                }
                # Abort the whole batch if this file doesn't exist.
                if(!-e "$dir/$line") {
                        die "Could not find $dir/$line. Uploading no files.\n"

                }
                $currentfile=$line;
                $desc_added=0;
                $dtw=0;$stw=0;
        }else {
                # If this is a header comment,
                # we just ignore it. Otherwise
                # it's a file description.
                if($currentfile) {
                        $file{$currentfile}.="\n".$line;
                        $desc_added=1;
                }
        }
}

# Last file needs to be processed, too
if($currentfile) {
        $dx= $dtw? $dtx-2 : $dtx -1;
        $sx= $stw? $stx-2 : $stx -1;
        if(!$desc_added) {
                $file{$currentfile}.="\n".$default_text[$dx];
        }
        $file{$currentfile}.="\n\n".$standard_text[$sx];
}

my $browser=LWP::UserAgent->new();
  my @ns_headers = (
   'User-Agent' => 'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7) Gecko/20041107 Firefox/1.0',
   'Accept' => 'image/gif, image/x-xbitmap, image/jpeg,
        image/pjpeg, image/png, */*',
   'Accept-Charset' => 'iso-8859-1,*,utf-8',
   'Accept-Language' => 'en-US',
  );

$browser->cookie_jar( {} );

$response=$browser->post("$cgi?title=Special:Userlogin&action=submitlogin",
@ns_headers, Content=>[wpName=>$username,wpPassword=>$password,wpRemember=>"1",wpLoginAttempt=>"Log in"]);

# After logging in, we should be redirected to another page.
# If we aren't, something is wrong.
#
if($response->code!=302 && !$ignore_login_error) {
        print
"We weren't able to login. This could have the following causes:

* The username ($username) or password may be incorrect.
  Solution: Edit upload.pl and change them.
* The MediaWiki software has been upgraded.
  Solution: Go to (where?)
  and get a new version of the upload script.
* You are trying to hack this script for other wikis. The wiki you
  are uploading to has cookie check disabled.
  Solution: Try setting \$ignore_login_error to 1.

Regardless, we will now try to write the output from the server to
$dir/debug.txt....\n\n";
        open(DEBUG,">$dir/debug.txt") or die "Could not write file.\n";
        print DEBUG $response->as_string;
        print
"This seems to have worked. Take a look at the file for further information.\n";
        close(DEBUG);
        exit 1;
}

foreach $key(keys(%file)) {
        sleep $pause;
        print "Uploading $key to the wiki $wiki. Description:\n";
        print $file{$key}."\n" . "-" x 75 . "\n";
        uploadfile:
        $eckey=encode('utf8',$key);
        if($eckey ne $key) {
                symlink("$key","$dir/$eckey");
        }
        $response=$browser->post("$cgi?title=Special:Upload",
        @ns_headers,Content_Type=>'form-data',Content=>
        [
                wpUploadFile=>["$dir/$eckey"],
                wpUploadDescription=>encode('utf8',$file{$key}),
                wpUploadAffirm=>"1",
                wpUpload=>"Upload file",
                wpIgnoreWarning=>"1"
        ]);
        push @responses,$response->as_string;
        if($response->code!=302 && $response->code!=200) {
                print "Upload failed! Will try again. Output was:\n";
                print $response->as_string;
                goto uploadfile;
        } else {
                print "Uploaded successfully.\n";
        }
}

print "Everything seems to be OK. Log will be written to $dir/debug.txt.\n";
open(DEBUG,">$dir/debug.txt") or die "Could not write file.\n";
print DEBUG @responses;
