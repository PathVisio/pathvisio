use warnings;
use strict;

use lib 'C:\\Documents and Settings\\thomas.kelder\\My Documents\\ensembl gdb\\ensembl\\modules';
#use strict;

use Bio::EnsEMBL::DBSQL::DBAdaptor;
use BIO::EnsEMBL::DBSQL::GeneAdaptor;
use BIO::EnsEMBL::DBEntry;
use BIO::EnsEMBL::Gene;


my $host		=	'localhost';
my $user		= 'root';
my $pass		= '0000';
my $dbname	= 'homo_sapiens_core_38_36';

my $db = new Bio::EnsEMBL::DBSQL::DBAdaptor(
-host 	=> $host,
-user 	=> $user,
-password => $pass,
-dbname => $dbname
);

my $ga = $db->get_GeneAdaptor();

print "gene adaptor loaded\n";

my $file = "ensembl_genes_start_to_21000.txt";
open OUTPUT,"> $file" or die "Unable to open outputfile \"$file\"\n";

my $progress;
$|=1;
my $genes = $ga->fetch_all;
foreach my $gene (@{$genes}[0..21000]) {
	$progress++;
	my $ens_id = $gene->stable_id;
	my $name = $gene->external_name;
	my $disp_id = $gene->display_id;
	my $descr = $gene->description;
	{
		no warnings;
		print OUTPUT "$ens_id\t$ens_id\tEnsembl\t$disp_id\t$name\t$descr\n";
	}
	my $output = "";
	my %processed_ids = ();
	foreach my $dbe (@{$gene->get_all_DBLinks}) {
		my $ext_id = $dbe->primary_id;
		my $disp_id = $dbe->display_id;
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