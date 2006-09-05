use strict;
use warnings;

package ListData;

=pod

=head1 NAME

ListData - tools for handling list of genes, similar to MAPPBuilder

=head1 SYNOPSIS

 use ListData;
 
 #convert pwf to tab delimited text format expected by mappfinder:
 $data = ListData::read_from_pwf ($filename1);
 $data->write_to_txt ($filename2);

=head1 METHODS

=over 

=item read_from_pwf

reads genes from a file from pwf format, 
and constructs a ListData object from that file.

example: 

my $data = ListData::read_from_pwf ($input_filename);

=cut

sub read_from_pwf
{
	my $fnIn = shift;	
	my %props;
	my %result;

		if (!open (INFILE, "< $fnIn"))
	{ 
		die "Error: Couldn't open $fnIn, $!\n"; 
	}
	
	#read the properties on the first three lines
	for my $prop (qw(PATHWAY_NAME PATHWAY_SOURCE PATHWAY_MARKER))
	{
		my $line = <INFILE>;
		chomp $line;
		if ($line =~ m#^//$prop = (.*)#)
		{
			$props{$prop} = $1;
		}
		else
		{
			die "Error: //$prop expected in $fnIn, line $.\n";
		}
	}

	$result{name} = $props{PATHWAY_NAME};
	
	#convert the pathway marker found in the .pwf file into genmapp's systemcode
	my %markerToSystemCode = 
	(
		ENSEMBL_GENE_ID => "En",
		HGNC => "H",
		UNIPROT => "S",
		ENTREZ => "L",
		UNIGENE => "U",
		AFFYMETRIX => "X",
		PDB_ID => "Pd",
		SGD_ID => "D"
	);
	
	if (exists $markerToSystemCode{$props{PATHWAY_MARKER}})
	{
		$result{systemcode} = $markerToSystemCode{$props{PATHWAY_MARKER}};
	}
	else
	{
		$result{systemcode} = "O";
		warn ("Unknown pathway marker '${props{PATHWAY_MARKER}}' " . 
			"found in $fnIn. Using systemcode 'O'\n");
	}
	
	#read gene list
	while (my $line = <INFILE>)
	{
		chomp $line;
		push @{$result{genes}}, $line;
	}
	
	close INFILE;
	
	bless \%result;
}

=pod

=item write_to_txt

Writes the data to a tab delimted text format understood by MappBuilder

example:

$data->write_to_txt ($output_filename);

=cut

sub write_to_txt
{
	my $self = shift;
	my $fnOut = shift;
	
	open (OUTFILE, "> $fnOut") or die "Couldn't open $fnOut for writing, $!\n";
	
	#print header line
	print OUTFILE join ("\t", qw(GeneID SystemCode Label Head Remarks MappName)), "\n";
	
	#print genes, one on each line.
	for my $gene (@{$$self{genes}})
	{
		print OUTFILE join ("\t", 
			$gene, $$self{systemcode}, $gene, "", "", $$self{name}), "\n"
	}
	
	close OUTFILE;
}

=pod

=back

=head1 INTERNALS

The structure of a ListData object is as follows:

 { 
   name => ..., # pathway name
   systemcode => ..., # e.g. En for Ensembl
   genes => [..., ..., ...] # list of genes
 }

=cut

1;
