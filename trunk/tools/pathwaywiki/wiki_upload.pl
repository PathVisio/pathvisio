#!perl -w
use strict;

# this script takes pairs of filename, description as parameters, and
# uploads them to the wiki.
#
# example:
# perl wiki-upload.pl image.jpg "first image" image2.jpg "second image"
#
# you need to create a file c:\local\etc\wiki.cfg
# containing 2 lines:
# user=USERNAME
# pass=PASSWORD
#
# copied from: http://morganreport.org/mediawiki/index.php?title=Upload_perl_script
#   - Martijn
#
#####################################
#   some included support nodules
package WebServer;
use strict;
use warnings;
use HTTP::Request::Common qw(POST GET);
use LWP::UserAgent;
use HTTP::Cookies;

use List::Util qw(first);

sub new {
    my ($class, $baseurl)= @_;

    my $ua= LWP::UserAgent->new(agent=>'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7) Gecko/20040501');
    $ua->cookie_jar(HTTP::Cookies->new(hide_cookie2=>1));
    $ua->env_proxy();

    return bless {
        ua=>$ua,
        baseurl=>$baseurl,
    }, $class;
}
sub clearcookies {
    my $self= shift;

    $self->{ua}->cookie_jar(HTTP::Cookies->new(hide_cookie2=>1));
}
# almost interface compatible with httpost
#   - optional hashref with parameters is merged with parameters.
#
# httpget("/some.cgi", key1=>123, key2=>455);
# httpget("/some.cgi", { urlkey1=>999 }, key1=>123, key2=>455);
#
sub httpget {
    my $self= shift;
    my $path= shift;

    my $query;
    if (@_) {
        $query= shift;
        if (ref $query ne "HASH") {
            unshift @_, $query;
            $query=undef;
        }
    }
    my %params= @_;

    my $uri= URI->new($self->{baseurl});
    $uri->path($path);
    $uri->query_form($query?%$query:(), %params);
    my $rq= GET $uri;

    # todo: get rid of 'TE' header, and 'Connection'-TE flag. and 'Cookie2' header
    $rq->header(
        'User-Agent'=> 'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7) Gecko/20040501',
        'Accept'=> ($path =~ /\.aspx|\.htm/ 
            ? 'text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5'
            : $path =~ /\.css/
            ? 'text/css,*/*;q=0.1'
            : '*/*') ,
        'Accept-Language'=> 'en-us,en;q=0.5',
        #'Accept-Encoding'=> 'gzip,deflate',
        'Accept-Charset'=> 'ISO-8859-1,utf-8;q=0.7,*;q=0.7',
    );
    #print "request:\n", $rq->as_string, "\n";
    #warn "network access disabled\n";
    #return;
    my $rp= $self->{ua}->request($rq) or die "httperror: $@\n";

    return $rp->content;
}

# can be called in several ways:
# httppost("/some.cgi", key1=>123, key2=>455);
#   -> just form values
# httppost("/some.cgi", { urlkey1=>999 }, key1=>123, key2=>455);
#   -> both url and form params
# httppost("/some.cgi", key1=>123, key2=>455, file1=>["filename"]);
#   -> form-data file upload
sub httppost {
    my $self= shift;
    my $path= shift;

    my $query;
    if (@_) {
        $query= shift;
        if (ref $query ne "HASH") {
            unshift @_, $query;
            $query=undef;
        }
    }
    my %params= @_;

    my $useformdata= grep { defined ref $_ && ref $_ eq "ARRAY" } values %params;

    my $uri= URI->new($self->{baseurl});
    $uri->path($path);
    $uri->query_form(%$query) if ($query);
    my $rq;
    if ( $useformdata ) {
        $rq = POST $uri, Content_Type=>"form-data", Content=>[ %params ];
    }
    else {
        $rq = POST $uri, [ %params ];
    }

    # -- for http uploads : 
    # ( Content_Type=>"form-data", Content=>[ %params ]);
    $rq->header(
        'User-Agent'=> 'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7) Gecko/20040501',
        'Accept'=> 'text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5',
        'Accept-Language'=> 'en-us,en;q=0.5',
        #'Accept-Encoding'=> 'gzip,deflate',
        'Accept-Charset'=> 'ISO-8859-1,utf-8;q=0.7,*;q=0.7',
    );
    #print "request:\n", $rq->as_string, "\n";
    #warn "network access disabled\n";
    #return;
    my $rp= $self->{ua}->request($rq) or die "httperror: $@\n";

    #print $rp->status_line, "\n";
    #print $rp->headers->as_string();

    return $rp->content;
}

sub httprequest {
    my ($self, $method, @params)= @_;
    if (lc($method) eq "get") {
        return $self->httpget(@params);
    }
    elsif (lc($method) eq "post") {
        return $self->httppost(@params);
    }
    else {
        die "invalid http request method '$method'\n";
    }
}



package MediaWiki;
use strict;
use warnings;
use HTML::TreeBuilder;

sub new {
    my $class= shift;

    my $self= bless {
        server=> WebServer->new("http://morganreport.org"),
        url=> "/mediawiki/index.php",
    }, $class;

    return $self;
}
sub post {
    my ($self, @params)= @_;
    $self->{server}->httppost($self->{url}, @params);
}
sub get {
    my ($self, @params)= @_;
    if (!$self->{loggedin}) {
        $self->login();
    }
    $self->{server}->httpget($self->{url}, @params);
}
sub cachedget {
    my ($self, @params)= @_;

    my $filename= encodeurlasfile(@params);

    if (-e $filename) {
        return readfile($filename);
    }

    my $data= $self->get(@params);
    savefile($filename, $data);
    return $data;
}
sub DESTROY {
    my $self= shift;
}
########################################################################


sub getwikisource {
    my ($self, $page)= @_;
    my $xml= $self->post(
        title=>'Special:Export', 
        action=>'submit',
        pages=>$page,
        curonly=>'true',
    );
    if ($xml =~ /<text[^>]*>(.*?)<\/text>/s) {
        return $1;
    }
    die "could not find <text> xml tag in\n$xml\n";
}
sub getcategoryitems {
    my ($self, $page)= @_;
    my $html= $self->get(
        title=>$page,
    );
    my $tree = HTML::TreeBuilder->new();
    $tree->parse($html);
    $tree->eof();

    my ($table)= $tree->look_down(
        "_tag"=>"table",
    ) or die "could not find category table for $page\n";

    my @items;
    for $a ($table->look_down("_tag", "a")) {
        push @items, $a->as_text();
    }
    return @items;
}
sub uploadfile {
    my ($self, $imgname, $imgdesc)= @_;
    $imgname =~ s/\\/\//g;
    ( my $imgdestname= $imgname ) =~ s/.*\///;

    my $answer= $self->post(
        { title=>'Special:Upload', },
        wpUploadFile=>[$imgname],
        wpDestFile=>$imgdestname,
        wpUploadDescription=>$imgdesc,
        wpUpload=>"Upload file",
    );
    
    print $answer->content;
}
sub logout {
    my $self= shift;
    my $answer= $self->get(title=>'Special:Userlogout');
    $self->{loggedin}= 0;
    $self->{server}->clearcookies();
}
sub login {
    my ($self, $username, $password)= @_;

    my $answer= $self->post(
        { action=>'submitlogin', title=>'Special:Userlogin' },
        wpName => $username,
        wpPassword => $password,
        wpLoginattempt => 'Log in',
    );
    $self->{loggedin}= 1;
}

# title=>'Template:UpcomingTable'
# action=>'submit'

# text   wpSummary
# flag   wpMinoredit 1
# flag   wpWatchthis
# button wpSave      Save page
# button wpPreview   Show preview
# button wpDiff      Show changes
# hidden wpSection
# hidden wpEdittime  20050730124636
# hidden wpEditToken cd44d6f6003e41d1d44b9a79266a846f
# text   wpTextbox1 

sub geteditform {
    my ($self, $page, $section)= @_;
    my $answer= $self->get(
        action=>'edit', 
        title=>$page,
        defined $section ? ( section=>$section ) : (),
    );
    my $tree = HTML::TreeBuilder->new();
    $tree->parse($answer);
    $tree->eof();

    my ($formtag)= $tree->look_down(
        "_tag"=>"form",
        "name"=>"editform",
    );
    my @inputelements= $formtag->look_down(
        "_tag"=>"input",
        sub { $_[0]->attr('type') ne 'submit' && $_[0]->attr('type') ne 'radio' }
    );
    my @textelements= $formtag->look_down(
        "_tag"=>"textarea",
    );


    my %form;
    # not handling radio buttons yet.
    for my $field (@inputelements) {
        $form{$field->attr('name')}= $field->attr('value')
    }
    for my $field (@textelements) {
        $form{$field->attr('name')}= $field->as_text;
    }
    return \%form;
}
sub saveeditform {
    my ($self, $page, $form)= @_;
    my $answer= $self->post(
        { action=>'submit', title=>$page, },
        wpSave=>"Save page",
        %$form,
    );
}

sub createpage {
    my ($self, $page, $content)= @_;

    my $f= $self->geteditform($page);
    if ($f->{wpTextbox1}) {
        print "----$page\n$f->{wpTextbox1}\n\n";
    }
    $f->{wpTextbox1}= $content;
    print map { sprintf("%-20s= %s\n", $_, defined $f->{$_} ? "'$f->{$_}'":"<undef>") } keys %$f;
    $self->saveeditform($page, $f);
}

package main;

use strict;
use warnings;
use IO::File;
$|=1;
my $m= MediaWiki->new();

if (@ARGV%2) {
    die "expected an even nr of params\n";
}
my $config= readconfig();
$m->login($config->{user}, $config->{pass});

for (my $i=0 ; $i<@ARGV ; $i+=2) {
    if (!-f $ARGV[$i]) {
        die "file $ARGV[$i] not found\n";
    }
    $m->uploadfile($ARGV[$i], $ARGV[$i+1]);
}
sub readconfig {
    my %params;
    my $fh= IO::File->new("wiki.cfg", "r") or die "wiki.cfg: $!";
    while (<$fh>) {
        s/\s+$//;
        if (/(\w+)\s*=\s*(.*)/) {
            my ($k, $v)= ($1, $2);
            $params{$k}= $v;
        }
    }
    $fh->close();
    return \%params;
}