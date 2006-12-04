#!/usr/bin/perl
#
# PathVisio,
# a tool for data visualization and analysis using Biological Pathways
# Copyright 2006-2007 BiGCaT Bioinformatics
#
# Licensed under the Apache License, Version 2.0 (the "License"); 
# you may not use this file except in compliance with the License. 
# You may obtain a copy of the License at 
# 
# http://www.apache.org/licenses/LICENSE-2.0 
#  
# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
# See the License for the specific language governing permissions and 
# limitations under the License.
#

use strict;
use Net::FTP;
use File::Find;

##################
#  globals
##################

my $default_conffile = "autoftp.conf";
my $conffile = $default_conffile;
my $timestampfile;

my $remotehost;
my $login;
my $pass;
my $local_basedir;
my $remote_basedir;

##################
#  subroutines
##################

# Read configuration file
sub read_config()
{
	open INFILE, "< $conffile" or die "Couldn't open $conffile, $!";
	
	while (my $line = <INFILE>)
	{
		#skip emties and comments...
		if ($line =~ /^\s*$/) { next; }
		if ($line =~ /^#/) { next; }
		
		if ($line =~ /^(.*?)\s*=\s*(.*)$/)
		{
			my ($field, $value) = ($1, $2);
			if    ($field eq "remotehost")      { $remotehost = $value; }
			elsif ($field eq "login")           { $login = $value; }
			elsif ($field eq "pass")            { $pass = $value; }
			elsif ($field eq "local_basedir")   { $local_basedir = $value; }
			elsif ($field eq "remote_basedir")  { $remote_basedir = $value; }
			elsif ($field eq "timestampfile")   { $timestampfile = $value; }
			else { die "Syntax error in configuration file, near $line"; }
		}
		else
		{
			die "Syntax error in configuration file, near $line";
		}
	}
	unless (defined $remotehost && defined $login && defined $pass && defined $local_basedir &&
		 defined $remote_basedir && defined $timestampfile)
	{	die "Configuration file misses certain configuration strings!"; }
	
	close INFILE;
}

##################
#  main program
##################

#use command line supplied configuration file if available
if (defined $ARGV[0] && -r $ARGV[0])
{
	$conffile = $ARGV[0];
}

read_config();

#get all files in the local copy of the website
my @local_files;

sub eachFile {
  push @local_files, $File::Find::name;
}
find (\&eachFile, $local_basedir);
chomp @local_files;
for (@local_files) { s#$local_basedir##; }

my $i;
#try to read previous timestamp from file

my $prevtimestamp;

if (-e ($timestampfile)) {
    open (TIMEFILE, $timestampfile) || die "Couldn't read previous timestamp";
    $prevtimestamp = <TIMEFILE>;
    chomp $prevtimestamp;
    close (TIMEFILE);
} else {
    $prevtimestamp = 0;
}

my $timestamp = time;

print "Previous timestamp set at: ", scalar (localtime $prevtimestamp), "\n";
print "Current time is: ", scalar (localtime $timestamp), "\n";

my @transferlist;

#filter local files...
#only non directories that have been modified since last timestamp file
foreach $i (@local_files)
{
    unless (-d ($local_basedir . $i))
    {
        my $mtime = (stat($local_basedir . $i))[9];
        if ($mtime > $prevtimestamp) {
            push @transferlist, $i;
        }
    }
}

print scalar @transferlist, " files to be copied\n";

if (scalar (@transferlist) == 0)
{
    print "done.\n";
    exit;
}

#now open ftp connection

my $ftp;

$ftp = Net::FTP->new($remotehost, Debug => 0)
  or die "Cannot connect to $remotehost: $@";
print "connected to $remotehost\n";

$ftp->login($login,$pass)
	  or die "Cannot login ", $ftp->message;

$ftp->cwd($remote_basedir)
	  or die "Cannot change working directory ", $ftp->message;

print "successfully logged in to $remotehost as $login\n";

foreach $i (@transferlist)
{
	if ($i =~ /\.(jar|jpg|jpeg|gif|zip|png|exe)$/i)
	{
		$ftp->binary();
		print "Binary: ";
	}
	elsif ($i =~ /\.(jnlp|txt|html|htm)$/i)
	{
		$ftp->ascii();
		print "Ascii: ";
	}
	$ftp->put($local_basedir . $i, $i)
		or die "Couldn't put file: $i - ", $ftp->message;
	
    print "copied $i\n";
}

$ftp->quit;

#write out current timestamp
print "writing timestamp... \n";
open (TIMEFILE, ">$timestampfile") || die "Couldn't write timestamp";
print TIMEFILE $timestamp;
close (TIMEFILE);

print "done.\n";
