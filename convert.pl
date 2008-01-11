use File::Find;
use warnings;
use strict;
use Cwd;

my $dir = shift(@ARGV);
my $ext_in = shift(@ARGV);
my $ext_out = shift(@ARGV);

print("Converting all $ext_in files in $dir to $ext_out files\n");

my $curr_dir = getcwd();

find(\&edits, $dir);

sub edits() {
	my $file = $_;
	if($file =~ /.$ext_in$/) {
		print "File name is $_\n\t\tFull path is $File::Find::name\n";
		my $newfile = $file;
		$newfile =~ s/.$ext_in$/.$ext_out/;
		my $cmd = "java -jar \"$curr_dir/pathvisio_core.jar\" \"$file\" \"$newfile\"";
		print("Executing: $cmd\n");
		system($cmd);
	}
}