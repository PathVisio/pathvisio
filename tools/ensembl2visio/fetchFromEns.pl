use     warnings;
use     strict;

#Load   the     Ensembl Perl API
use lib 'ensPerl/ensembl/modules';
use     Bio::EnsEMBL::Registry;
use     Bio::EnsEMBL::DBSQL::GeneAdaptor;
use     Bio::EnsEMBL::DBEntry;
use     Bio::EnsEMBL::Gene;
use     Getopt::Long;

my $organism;
my $fnOutput;

my $result = GetOptions (
	"organism=s" => \$organism,
	"output=s" => \$fnOutput,
);

##Use this when connecting to local database
#my $host		= 'localhost';
#my $user		= 'root';
#my $pass		= '0000';
#my $dbname = $databases{$organism};
#
## Connect to the database
#my $db = new Bio::EnsEMBL::DBSQL::DBAdaptor(
#-host 	=> $host,
#-user 	=> $user,
#-password => $pass,
#-dbname => $dbname
#);

my  $reg = "Bio::EnsEMBL::Registry";
$reg->load_registry_from_db(
	 -host => "ensembldb.ensembl.org",
	 -user => "anonymous",);

# obtain a gene adaptor from the registry
my $ga = $reg->get_adaptor($organism,"core","gene");

print "gene adaptor loaded\n";

# Output file
my $file = "$organism/$dbname.txt";
open OUTPUT,"> $file" or die "Unable to open outputfile \"$file\"\n";

my $progress;
$|=1;
# Get all ensembl genes at once
my $genes = $ga->fetch_all;
# Process genes
foreach my $gene (@{$genes}) {
	$progress++;
	# Get data for this Ensembl gene
	my $ens_id = $gene->stable_id;
	my $name = $gene->external_name;
	my $disp_id = $gene->display_id;
	my $descr = $gene->description;

	# Some fields may be undefined, so supress warnings
	{
		no warnings;
		print OUTPUT "$ens_id\t$ens_id\tEnsembl\t$disp_id\t$name\t$descr\n";
	}
	my $output = "";
	my %processed_ids = ();
	# Process all cross-references to non-ensembl databases
	foreach my $dbe (@{$gene->get_all_DBLinks}) {
		my $ext_id = $dbe->primary_id;
		my $disp_id = $dbe->display_id;
		# Check if we processed this cross-reference before
		if( !(exists $processed_ids{$ext_id}) ) {
			$processed_ids{$ext_id} = undef;
			my $dbname = $dbe->db_display_name;
			my $ext_descr = $dbe->description;
			if(!$ext_descr) {
				$ext_descr = $descr;
			}
			{
				no warnings;
				$output = $output."$ens_id\t$ext_id\t$dbname\t$disp_id\t$name\t$ext_descr\n";
			}
		} else {
			next;
		}
	}
	print OUTPUT $output;
	if(($progress % 100) == 0) {
		print "$progress genes processed\n";
	}
}
print " $progress genes total";
