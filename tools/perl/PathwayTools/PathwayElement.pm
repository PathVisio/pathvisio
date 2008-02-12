use warnings;
use strict;

package PathwayTools::PathwayElement;

my $NS = "http://genmapp.org/GPML/2007";

sub new
{
	my $class = shift;
	my %specs = @_;

	my %defaults = 
	(
		DataNode => {
			centerx => 0,
			centery => 0,
			width => 900,
			height => 300,
			color => "Transparent",
			type => "Unknown",
			textlabel => "Gene",
			backpagehead => "",
			database => "",
			id => "",
		},
		Label => {
			centerx => 0,
			centery => 0,
			width => 900,
			height => 300,
			textlabel => "",
			color => "Black",
			fontweight => "Bold", 
			fontsize => "150",
		},
		
	);
	
	my $self = {};
	
	my $type = $specs{element};
	
	if (!exists $defaults{$type})
	{
		die "incorrect or unimplemented type: '" .  
			(defined $type ? $type : "undef") . 
			"' specified for new PathwayElement";
	}
	
	for my $key (keys %{$defaults{$type}})
	{
		$self->{$key} = (exists $specs{$key} ? $specs{$key} : $defaults{DataNode}->{$key});
	}
	$self->{element} = $type;
		
	bless $self, $class;
}

sub get_xml_node
{
	my $self = shift;
	if ($self->{element} eq "DataNode")
	{
		my $datanode = XML::LibXML::Element->new("DataNode");
		$datanode->setNamespace ($NS);
		$datanode->setAttribute ("TextLabel", $self->{textlabel});
		$datanode->setAttribute ("Type", $self->{type});
		$datanode->setAttribute ("BackpageHead", $self->{backpagehead});
		
		my $graphics = $datanode->addNewChild ($NS, "Graphics");
		$graphics->setAttribute ("Color", $self->{color});
		$graphics->setAttribute ("CenterX", $self->{centerx});
		$graphics->setAttribute ("CenterY", $self->{centery});
		$graphics->setAttribute ("Width", $self->{width});
		$graphics->setAttribute ("Height", $self->{height});
		
		my $xref = $datanode->addNewChild ($NS, "Xref");
		$xref->setAttribute ("Database", $self->{database});
		$xref->setAttribute ("ID", $self->{id});
		
		return $datanode;
	}
	elsif ($self->{element} eq "Label")
	{
		my $elt = XML::LibXML::Element->new("Label");
		$elt ->setNamespace ($NS);
		$elt->setAttribute ("TextLabel", $self->{textlabel});
		
		my $graphics = $elt->addNewChild ($NS, "Graphics");
		$graphics->setAttribute ("Color", $self->{color});
		$graphics->setAttribute ("CenterX", $self->{centerx});
		$graphics->setAttribute ("CenterY", $self->{centery});
		$graphics->setAttribute ("Width", $self->{width});
		$graphics->setAttribute ("Height", $self->{height});
		$graphics->setAttribute ("FontWeight", $self->{fontweight});
		$graphics->setAttribute ("FontSize", $self->{fontsize});
		
		return $elt;
	}
	else
	{
		die "Unknown or unimplemented PathwayElement type";
	}
}

1;
