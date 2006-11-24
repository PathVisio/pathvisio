#!/usr/bin/perl
#
#  

use strict;
use warnings;

use Data::Dumper;
use Win32::ODBC;
use DBI;
use File::Basename;

######################
#
######################

my $fnGdb = "d:/GenMAPP data/Tutorial-Database.gdb";
my $fnGex = "d:/GenMAPP data/Tutorial-ExpressionData.gex";
my $fnGpml = "d:/prg/gmml-visio/trunk/gmml_mapp_examples/Hs_Apoptosis.xml";
my $fnGdbNew = "d:/GenMAPP data/Tutorial-Database-converted.mdb";

######################
#
######################

open GPML, "$fnGpml" or die;
my @genes;

my %systemCodes = (
	"SGD"          => "D",
	"FlyBase"      => "F",
	"GenBank"      => "G",
	"InterPro"     => "I",
	"LocusLink"    => "L",
	"MGI"          => "M",
	"RefSeq"       => "Q",
	"RGD"          => "R",
	"SwissProt"    => "S",
	"GeneOntology" => "T",
	"UniGene"      => "U",
	"WormBase"     => "W",
	"ZFIN"         => "Z",
	"Affy"         => "X",
	"Other"        => "O",
);

#extract genes from gpml mapps, the easy way, without bothering 
#about xml and all that
for (<GPML>)
{
	if (/<GeneProduct BackpageHead="(.*)" GeneID="(.*)" GeneProduct-Data-Source="(.*)" Type="(.*)" Xref="(.*)" Name="(.*)">/)
	{
		my ($id, $code) = ($2, $3);
		if (exists ($systemCodes{$code}))
		{
			$code = $systemCodes{$code};
		}
		else { die; }
		push @genes, [$code, $id];
	}
}

close GPML;

print "Info:  Read ", scalar @genes, " gene id's from ", basename($fnGpml), "\n";

my $dbhGdb;

print "Info:  Connecting to Gdb ", basename($fnGdb), "\n";
# connect to gdb
{
	my $odbc_dsn = "temp_dsn_for_gdb";
	
	Win32::ODBC::ConfigDSN(
		Win32::ODBC::ODBC_ADD_DSN, 
		"Microsoft Access Driver (*.mdb)",
		"DSN=" . $odbc_dsn,
		"DBQ=" . $fnGdb
	) or die $!;
	
	my $dbi_dsn = "DBI:ODBC:" . $odbc_dsn;
	$dbhGdb = DBI->connect( $dbi_dsn );
	unless ($dbhGdb) { die; }
}

my $dbhGex;

print "Info:  Connecting to Gex ", basename($fnGex), "\n";
#connect to gex
{
	my $odbc_dsn = "temp_dsn_for_gex";
	
	Win32::ODBC::ConfigDSN(
		Win32::ODBC::ODBC_ADD_DSN, 
		"Microsoft Access Driver (*.mdb)",
		"DSN=" . $odbc_dsn,
		"DBQ=" . $fnGex
	) or die $!;
	
	my $dbi_dsn = "DBI:ODBC:" . $odbc_dsn;
	$dbhGex = DBI->connect( $dbi_dsn );
	unless ($dbhGex) { die; }
}

print "Info:  Creating converted Gene Database at ", basename ($fnGdbNew), "\n";

# copy template to output file

my $retval = system ("copy", "template.mdb", $fnGdbNew);
print "Debug:  copy returned with $retval\n";

my $dbhGdbNew;
#connect to mdb
{
	my $odbc_dsn = "temp_dsn_for_mdb";
	
	Win32::ODBC::ConfigDSN(
		Win32::ODBC::ODBC_ADD_DSN, 
		"Microsoft Access Driver (*.mdb)",
		"DSN=" . $odbc_dsn,
		"DBQ=" . $fnGdbNew
	) or die $!;
	
	my $dbi_dsn = "DBI:ODBC:" . $odbc_dsn;
	$dbhGdbNew = DBI->connect( $dbi_dsn );
	unless ($dbhGdbNew) { die; }
}


# create other data tables
# read relations table, for each row, access table and transfer info

$dbhGdb->{LongReadLen} = 512;
print "Debug: LongTruncOk: ", $dbhGdb->{LongTruncOk}, " LongReadLen: ", $dbhGdb->{LongReadLen}, "\n"; 

my $sth = $dbhGdb->prepare("SELECT * FROM relations");
$sth->execute() or die "SQL error: $DBI::errstr";

my $sthInsert = $dbhGdbNew->prepare ("
	INSERT INTO 
		link 
		(idLeft, codeLeft, idRight, codeRight, bridge) 
	VALUES (?, ?, ?, ?, ?)
	");
	
while (my $row = $sth->fetchrow_hashref)
{
	my $codeLeft = $$row{"SystemCode"};
	my $codeRight = $$row{"RelatedCode"};
	my $tableName = $$row{"Relation"};
	
	print "Debug: $tableName\n";
	my $sql = "SELECT * FROM `$tableName`";
	print "Debug: '$sql'\n";
	my $sthFetchLink = $dbhGdb->prepare ($sql);
	$sthFetchLink->execute() or die "SQL error $DBI::errstr";
	
	while (my $row2 = $sthFetchLink->fetchrow_hashref)
	{
		my $idLeft = $$row2{"Primary"};
		my $idRight = $$row2{"Related"};
		my $bridge = $$row2{"Bridge"};
		
		$sthInsert->execute ($idLeft, $codeLeft, $idRight, $codeRight, $bridge);
	}
	if ($sthFetchLink->err) { die $sthFetchLink->err; }

}
if ($sth->err) { die $sth->err; }
