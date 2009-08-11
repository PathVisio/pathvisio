# Generate a bridge.config file for the most recent synonym databases, based on a set of .pdgb/.bridge files
# in a directory.
# You need a bridge.config file for several tools, such as the lucene indexer and wikipathways bots.
use strict;
use HashSpeciesList;
use Getopt::Std;

# Parse command line arguments
sub printUsage() {
	die "Use: perl GenerateGdbConfig.pl -i path_to_databases -o output_file\n";
}

my %opt;
getopt('io', \%opt);
my $bridgePath = $opt{'i'};
my $out = $opt{'o'};

if(!$bridgePath) {
	print "Missing path to bridgedb databases directory.\n";
	printUsage();
}
if(!$out) {
	print "Missing output file.\n";
	printUsage();
}

my %speciesTable = getSpeciesTable();

# Create hash to find species by code
my %orgByCode;
for my $key (keys %speciesTable) {
	my $code = $speciesTable{$key}[3];
	my $org = $speciesTable{$key}[0];
	$orgByCode{$code} = $key;
}

# List all bridgedb files
opendir(DIR, $bridgePath);
my @bridgeFiles = grep(/\.(bridge|pgdb)$/,readdir(DIR));
closedir(DIR);

# Key is organism, value is hash of date->file
my %filesByOrganism;

for my $file (@bridgeFiles) {
	print "Parsing $file\n";
	# Assumes file name like: Mm_Derby_20090509 or metabolites_20090509
	# Parse date
	my $date;
	my $org;
	
	if($file =~ m/(\d{8}|\d{6})/) {
		$date = $1;
		if(length($date) == 6) {
			$date = "20$date";
		}
	} else {
		die "Unable to parse date from bridgedb database file name: $file\n";
	}
	# Parse organism
	if($file =~ m/^([A-Z]{1}[a-z]{1})_Derby/) {
		$org = $orgByCode{$1};
		$org or die "Unable to find organism for code $1\n";
	} elsif($file =~ m/metabolites/) {
		$org = "*";
	} else {
		die "Unable to parse organism code from bridgedb database file name: $file\n";
	}
	$filesByOrganism{$org}{$date} = $file;
}

#Now print the bridge.conf file to stdout
if(!($bridgePath =~ /\/$/)) {
	$bridgePath = "$bridgePath/";
}
print "Writing $out\n";
open OUT, ">$out" or die "Unable to write to output file $out\n";
for my $org (keys %filesByOrganism) {
	my @dates = keys %{$filesByOrganism{$org}};
	@dates = sort { $a <=> $b } @dates;
	my $newest = $dates[-1];
	my $file = $filesByOrganism{$org}{$newest};
	$file = "$bridgePath$file";
	print OUT "$org\t$file\n";
}

