use Devel::Size qw(size total_size);
use DBI;
use lib '/home/apico/src/ensembl/modules';
use lib '/home/apico/bioperl-live'; 
use Bio::EnsEMBL::Registry;
use Bio::EnsEMBL::DBSQL::DBConnection;

Bio::EnsEMBL::Registry->load_registry_from_db(
        -host => 'ensembldb.ensembl.org',
        -user => 'anonymous',
        -verbose => "0");

my $species = 'Mus musculus';
my $gene_adaptor = Bio::EnsEMBL::Registry->get_adaptor($species, "core", "gene");
my @dbas = @{Bio::EnsEMBL::Registry->get_all_DBAdaptors(-species => $species)};
my $dbname = $dbas[0]->dbc->dbname();

print "\nData for $species extracted from $dbname\n";

my $count = 1;

# FOREACH GENE
foreach my $gene (@{$gene_adaptor->fetch_all()})
{

    print "Processing gene $count\n";
    my $gene_stable_id = $gene->stable_id();
    parse_DBLinks($gene->get_all_DBLinks());
    parse_Trans($gene->get_all_Transcripts());

    $count++;
    
    # NEEDED TO FREE OBJECT MEMORY?
#    $gene = 0;

} 

print "\nDONE\n";

exit;

# SUBROUTINES
sub parse_DBLinks {
    my $dblinks = @_;

    foreach my $dbe (@$dblinks){
	my $dbe_display = $dbe->display_id();
    }
}

sub parse_Trans {
    my $all_Transcripts = @_;

    foreach my $trans (@$all_Transcripts) {
        my $trans_stable_id = $trans->stable_id();
        if ($trans->translateable_seq()){
	    my $desc = $trans->description();
            foreach my $exon (@{$trans->get_all_Exons()}) {
		my $exon_stable_id = $exon->stable_id();
            }
            if($trans->translation()) {
                my $transln = $trans->translation();
                my $transln_stable_id = $transln->stable_id();
                my $protein_feats = $transln->get_all_ProteinFeatures(); 
                foreach my $pf (@$protein_feats) {
                    my @genomic = $trans->pep2genomic($pf->start(), $pf->end());
		}
	    }
	}
    }
}
