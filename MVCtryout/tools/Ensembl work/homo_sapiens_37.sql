-- MySQL dump 10.9
--
-- Host: ecs3    Database: homo_sapiens_core_37_35j
-- ------------------------------------------------------
-- Server version	4.1.12-log
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO,MYSQL40' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `affy_array`
--

DROP TABLE IF EXISTS `affy_array`;
CREATE TABLE `affy_array` (
  `affy_array_id` int(11) NOT NULL auto_increment,
  `parent_array_id` int(11) default NULL,
  `probe_setsize` tinyint(4) NOT NULL default '0',
  `name` varchar(40) NOT NULL default '',
  PRIMARY KEY  (`affy_array_id`)
) TYPE=MyISAM;

--
-- Table structure for table `affy_feature`
--

DROP TABLE IF EXISTS `affy_feature`;
CREATE TABLE `affy_feature` (
  `affy_feature_id` int(11) NOT NULL auto_increment,
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(11) NOT NULL default '0',
  `seq_region_end` int(11) NOT NULL default '0',
  `seq_region_strand` tinyint(4) NOT NULL default '0',
  `mismatches` tinyint(4) default NULL,
  `affy_probe_id` int(11) NOT NULL default '0',
  `analysis_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`affy_feature_id`),
  KEY `seq_region_idx` (`seq_region_id`,`seq_region_start`),
  KEY `probe_idx` (`affy_probe_id`)
) TYPE=MyISAM;

--
-- Table structure for table `affy_probe`
--

DROP TABLE IF EXISTS `affy_probe`;
CREATE TABLE `affy_probe` (
  `affy_probe_id` int(11) NOT NULL auto_increment,
  `affy_array_id` int(11) NOT NULL default '0',
  `probeset` varchar(40) default NULL,
  `name` varchar(20) default NULL,
  PRIMARY KEY  (`affy_probe_id`,`affy_array_id`),
  KEY `probeset_idx` (`probeset`),
  KEY `array_idx` (`affy_array_id`)
) TYPE=MyISAM;

--
-- Table structure for table `alt_allele`
--

DROP TABLE IF EXISTS `alt_allele`;
CREATE TABLE `alt_allele` (
  `alt_allele_id` int(11) NOT NULL auto_increment,
  `gene_id` int(11) NOT NULL default '0',
  UNIQUE KEY `gene_idx` (`gene_id`),
  UNIQUE KEY `allele_idx` (`alt_allele_id`,`gene_id`)
) TYPE=MyISAM;

--
-- Table structure for table `analysis`
--

DROP TABLE IF EXISTS `analysis`;
CREATE TABLE `analysis` (
  `analysis_id` int(10) unsigned NOT NULL auto_increment,
  `created` datetime NOT NULL default '0000-00-00 00:00:00',
  `logic_name` varchar(40) NOT NULL default '',
  `db` varchar(120) default NULL,
  `db_version` varchar(40) default NULL,
  `db_file` varchar(120) default NULL,
  `program` varchar(80) default NULL,
  `program_version` varchar(40) default NULL,
  `program_file` varchar(80) default NULL,
  `parameters` varchar(255) default NULL,
  `module` varchar(80) default NULL,
  `module_version` varchar(40) default NULL,
  `gff_source` varchar(40) default NULL,
  `gff_feature` varchar(40) default NULL,
  PRIMARY KEY  (`analysis_id`),
  UNIQUE KEY `logic_name` (`logic_name`),
  KEY `logic_name_idx` (`logic_name`)
) TYPE=MyISAM;

--
-- Table structure for table `analysis_description`
--

DROP TABLE IF EXISTS `analysis_description`;
CREATE TABLE `analysis_description` (
  `analysis_id` int(10) unsigned NOT NULL default '0',
  `description` text,
  `display_label` varchar(255) default NULL,
  KEY `analysis_idx` (`analysis_id`)
) TYPE=MyISAM;

--
-- Table structure for table `assembly`
--

DROP TABLE IF EXISTS `assembly`;
CREATE TABLE `assembly` (
  `asm_seq_region_id` int(10) unsigned NOT NULL default '0',
  `cmp_seq_region_id` int(10) unsigned NOT NULL default '0',
  `asm_start` int(10) NOT NULL default '0',
  `asm_end` int(10) NOT NULL default '0',
  `cmp_start` int(10) NOT NULL default '0',
  `cmp_end` int(10) NOT NULL default '0',
  `ori` tinyint(4) NOT NULL default '0',
  KEY `cmp_seq_region_id` (`cmp_seq_region_id`),
  KEY `asm_seq_region_id` (`asm_seq_region_id`,`asm_start`)
) TYPE=MyISAM;

--
-- Table structure for table `assembly_exception`
--

DROP TABLE IF EXISTS `assembly_exception`;
CREATE TABLE `assembly_exception` (
  `assembly_exception_id` int(10) unsigned NOT NULL auto_increment,
  `seq_region_id` int(11) NOT NULL default '0',
  `seq_region_start` int(11) NOT NULL default '0',
  `seq_region_end` int(11) NOT NULL default '0',
  `exc_type` enum('HAP','PAR') NOT NULL default 'HAP',
  `exc_seq_region_id` int(11) NOT NULL default '0',
  `exc_seq_region_start` int(11) NOT NULL default '0',
  `exc_seq_region_end` int(11) NOT NULL default '0',
  `ori` int(11) NOT NULL default '0',
  PRIMARY KEY  (`assembly_exception_id`),
  KEY `sr_idx` (`seq_region_id`,`seq_region_start`),
  KEY `ex_idx` (`exc_seq_region_id`,`exc_seq_region_start`)
) TYPE=MyISAM;

--
-- Table structure for table `attrib_type`
--

DROP TABLE IF EXISTS `attrib_type`;
CREATE TABLE `attrib_type` (
  `attrib_type_id` smallint(5) unsigned NOT NULL auto_increment,
  `code` varchar(15) NOT NULL default '',
  `name` varchar(255) NOT NULL default '',
  `description` text,
  PRIMARY KEY  (`attrib_type_id`),
  UNIQUE KEY `c` (`code`)
) TYPE=MyISAM;

--
-- Table structure for table `coord_system`
--

DROP TABLE IF EXISTS `coord_system`;
CREATE TABLE `coord_system` (
  `coord_system_id` int(11) NOT NULL auto_increment,
  `name` varchar(40) NOT NULL default '',
  `version` varchar(40) default NULL,
  `rank` int(11) NOT NULL default '0',
  `attrib` set('default_version','sequence_level') default NULL,
  PRIMARY KEY  (`coord_system_id`),
  UNIQUE KEY `rank` (`rank`),
  UNIQUE KEY `name` (`name`,`version`)
) TYPE=MyISAM;

--
-- Table structure for table `density_feature`
--

DROP TABLE IF EXISTS `density_feature`;
CREATE TABLE `density_feature` (
  `density_feature_id` int(11) NOT NULL auto_increment,
  `density_type_id` int(11) NOT NULL default '0',
  `seq_region_id` int(11) NOT NULL default '0',
  `seq_region_start` int(11) NOT NULL default '0',
  `seq_region_end` int(11) NOT NULL default '0',
  `density_value` float NOT NULL default '0',
  PRIMARY KEY  (`density_feature_id`),
  KEY `seq_region_idx` (`density_type_id`,`seq_region_id`,`seq_region_start`),
  KEY `seq_region_id_idx` (`seq_region_id`)
) TYPE=MyISAM;

--
-- Table structure for table `density_type`
--

DROP TABLE IF EXISTS `density_type`;
CREATE TABLE `density_type` (
  `density_type_id` int(11) NOT NULL auto_increment,
  `analysis_id` int(11) NOT NULL default '0',
  `block_size` int(11) NOT NULL default '0',
  `region_features` int(11) NOT NULL default '0',
  `value_type` enum('sum','ratio') NOT NULL default 'sum',
  PRIMARY KEY  (`density_type_id`),
  UNIQUE KEY `analysis_id` (`analysis_id`,`block_size`,`region_features`)
) TYPE=MyISAM;

--
-- Table structure for table `dna`
--

DROP TABLE IF EXISTS `dna`;
CREATE TABLE `dna` (
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `sequence` mediumtext NOT NULL,
  PRIMARY KEY  (`seq_region_id`)
) TYPE=MyISAM MAX_ROWS=750000 AVG_ROW_LENGTH=19000;

--
-- Table structure for table `dna_align_feature`
--

DROP TABLE IF EXISTS `dna_align_feature`;
CREATE TABLE `dna_align_feature` (
  `dna_align_feature_id` int(10) unsigned NOT NULL auto_increment,
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(10) unsigned NOT NULL default '0',
  `seq_region_end` int(10) unsigned NOT NULL default '0',
  `seq_region_strand` tinyint(1) NOT NULL default '0',
  `hit_start` int(11) NOT NULL default '0',
  `hit_end` int(11) NOT NULL default '0',
  `hit_strand` tinyint(1) NOT NULL default '0',
  `hit_name` varchar(40) NOT NULL default '',
  `analysis_id` int(10) unsigned NOT NULL default '0',
  `score` double default NULL,
  `evalue` double default NULL,
  `perc_ident` float default NULL,
  `cigar_line` text,
  PRIMARY KEY  (`dna_align_feature_id`),
  KEY `analysis_idx` (`analysis_id`),
  KEY `hit_idx` (`hit_name`),
  KEY `seq_region_idx_2` (`seq_region_id`,`seq_region_start`),
  KEY `seq_region_idx` (`seq_region_id`,`analysis_id`,`seq_region_start`,`score`)
) TYPE=MyISAM MAX_ROWS=100000000 AVG_ROW_LENGTH=80;

--
-- Table structure for table `dnac`
--

DROP TABLE IF EXISTS `dnac`;
CREATE TABLE `dnac` (
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `sequence` mediumblob NOT NULL,
  `n_line` text,
  PRIMARY KEY  (`seq_region_id`)
) TYPE=MyISAM MAX_ROWS=750000 AVG_ROW_LENGTH=19000;

--
-- Table structure for table `exon`
--

DROP TABLE IF EXISTS `exon`;
CREATE TABLE `exon` (
  `exon_id` int(10) unsigned NOT NULL auto_increment,
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(10) unsigned NOT NULL default '0',
  `seq_region_end` int(10) unsigned NOT NULL default '0',
  `seq_region_strand` tinyint(2) NOT NULL default '0',
  `phase` tinyint(2) NOT NULL default '0',
  `end_phase` tinyint(2) NOT NULL default '0',
  PRIMARY KEY  (`exon_id`),
  KEY `seq_region_idx` (`seq_region_id`,`seq_region_start`)
) TYPE=MyISAM;

--
-- Table structure for table `exon_stable_id`
--

DROP TABLE IF EXISTS `exon_stable_id`;
CREATE TABLE `exon_stable_id` (
  `exon_id` int(10) unsigned NOT NULL default '0',
  `stable_id` varchar(128) NOT NULL default '',
  `version` int(10) default NULL,
  `created_date` datetime NOT NULL default '0000-00-00 00:00:00',
  `modified_date` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`exon_id`),
  UNIQUE KEY `stable_id` (`stable_id`,`version`)
) TYPE=MyISAM;

--
-- Table structure for table `exon_transcript`
--

DROP TABLE IF EXISTS `exon_transcript`;
CREATE TABLE `exon_transcript` (
  `exon_id` int(10) unsigned NOT NULL default '0',
  `transcript_id` int(10) unsigned NOT NULL default '0',
  `rank` int(10) NOT NULL default '0',
  PRIMARY KEY  (`exon_id`,`transcript_id`,`rank`),
  KEY `transcript` (`transcript_id`),
  KEY `exon` (`exon_id`)
) TYPE=MyISAM;

--
-- Table structure for table `external_db`
--

DROP TABLE IF EXISTS `external_db`;
CREATE TABLE `external_db` (
  `external_db_id` int(11) NOT NULL default '0',
  `db_name` varchar(27) NOT NULL default '',
  `release` varchar(40) NOT NULL default '',
  `status` enum('KNOWNXREF','KNOWN','XREF','PRED','ORTH','PSEUDO') NOT NULL default 'KNOWNXREF',
  `dbprimary_acc_linkable` tinyint(1) NOT NULL default '1',
  `display_label_linkable` tinyint(1) NOT NULL default '0',
  `priority` int(11) NOT NULL default '0',
  `db_display_name` varchar(255) default NULL,
  PRIMARY KEY  (`external_db_id`)
) TYPE=MyISAM;

--
-- Table structure for table `external_synonym`
--

DROP TABLE IF EXISTS `external_synonym`;
CREATE TABLE `external_synonym` (
  `xref_id` int(10) unsigned NOT NULL default '0',
  `synonym` varchar(40) NOT NULL default '',
  PRIMARY KEY  (`xref_id`,`synonym`),
  KEY `name_index` (`synonym`)
) TYPE=MyISAM;

--
-- Table structure for table `gene`
--

DROP TABLE IF EXISTS `gene`;
CREATE TABLE `gene` (
  `gene_id` int(10) unsigned NOT NULL auto_increment,
  `biotype` varchar(40) NOT NULL default 'protein_coding',
  `analysis_id` int(11) default NULL,
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(10) unsigned NOT NULL default '0',
  `seq_region_end` int(10) unsigned NOT NULL default '0',
  `seq_region_strand` tinyint(2) NOT NULL default '0',
  `display_xref_id` int(10) unsigned default NULL,
  `source` varchar(20) NOT NULL default 'ensembl',
  `status` enum('KNOWN','NOVEL','PUTATIVE','PREDICTED') default NULL,
  `description` text,
  PRIMARY KEY  (`gene_id`),
  KEY `seq_region_idx` (`seq_region_id`,`seq_region_start`),
  KEY `xref_id_index` (`display_xref_id`),
  KEY `analysis_idx` (`analysis_id`)
) TYPE=MyISAM;

--
-- Table structure for table `gene_archive`
--

DROP TABLE IF EXISTS `gene_archive`;
CREATE TABLE `gene_archive` (
  `gene_stable_id` varchar(128) NOT NULL default '',
  `gene_version` smallint(6) NOT NULL default '0',
  `transcript_stable_id` varchar(128) NOT NULL default '',
  `transcript_version` smallint(6) NOT NULL default '0',
  `translation_stable_id` varchar(128) NOT NULL default '',
  `translation_version` smallint(6) NOT NULL default '0',
  `peptide_archive_id` int(11) NOT NULL default '0',
  `mapping_session_id` int(11) NOT NULL default '0',
  KEY `gene_idx` (`gene_stable_id`,`gene_version`),
  KEY `transcript_idx` (`transcript_stable_id`,`transcript_version`),
  KEY `translation_idx` (`translation_stable_id`,`translation_version`)
) TYPE=MyISAM;

--
-- Table structure for table `gene_attrib`
--

DROP TABLE IF EXISTS `gene_attrib`;
CREATE TABLE `gene_attrib` (
  `gene_id` int(10) unsigned NOT NULL default '0',
  `attrib_type_id` smallint(5) unsigned NOT NULL default '0',
  `value` varchar(255) NOT NULL default '',
  KEY `type_val_idx` (`attrib_type_id`,`value`),
  KEY `gene_idx` (`gene_id`)
) TYPE=MyISAM;

--
-- Table structure for table `gene_stable_id`
--

DROP TABLE IF EXISTS `gene_stable_id`;
CREATE TABLE `gene_stable_id` (
  `gene_id` int(10) unsigned NOT NULL default '0',
  `stable_id` varchar(128) NOT NULL default '',
  `version` int(10) default NULL,
  `created_date` datetime NOT NULL default '0000-00-00 00:00:00',
  `modified_date` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`gene_id`),
  UNIQUE KEY `stable_id` (`stable_id`,`version`)
) TYPE=MyISAM;

--
-- Table structure for table `go_xref`
--

DROP TABLE IF EXISTS `go_xref`;
CREATE TABLE `go_xref` (
  `object_xref_id` int(10) unsigned NOT NULL default '0',
  `linkage_type` enum('IC','IDA','IEA','IEP','IGI','IMP','IPI','ISS','NAS','ND','TAS','NR','RCA') default NULL,
  UNIQUE KEY `object_xref_id_2` (`object_xref_id`,`linkage_type`),
  KEY `object_xref_id` (`object_xref_id`)
) TYPE=MyISAM;

--
-- Table structure for table `identity_xref`
--

DROP TABLE IF EXISTS `identity_xref`;
CREATE TABLE `identity_xref` (
  `object_xref_id` int(10) unsigned NOT NULL default '0',
  `query_identity` int(5) default NULL,
  `target_identity` int(5) default NULL,
  `hit_start` int(11) default NULL,
  `hit_end` int(11) default NULL,
  `translation_start` int(11) default NULL,
  `translation_end` int(11) default NULL,
  `cigar_line` text,
  `score` double default NULL,
  `evalue` double default NULL,
  `analysis_id` int(11) default NULL,
  PRIMARY KEY  (`object_xref_id`),
  KEY `analysis_idx` (`analysis_id`)
) TYPE=MyISAM;

--
-- Table structure for table `interpro`
--

DROP TABLE IF EXISTS `interpro`;
CREATE TABLE `interpro` (
  `interpro_ac` varchar(40) NOT NULL default '',
  `id` varchar(40) NOT NULL default '',
  UNIQUE KEY `interpro_ac` (`interpro_ac`,`id`),
  KEY `id` (`id`)
) TYPE=MyISAM;

--
-- Table structure for table `karyotype`
--

DROP TABLE IF EXISTS `karyotype`;
CREATE TABLE `karyotype` (
  `karyotype_id` int(10) unsigned NOT NULL auto_increment,
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(10) NOT NULL default '0',
  `seq_region_end` int(10) NOT NULL default '0',
  `band` varchar(40) NOT NULL default '',
  `stain` varchar(40) NOT NULL default '',
  PRIMARY KEY  (`karyotype_id`),
  KEY `region_band_idx` (`seq_region_id`,`band`)
) TYPE=MyISAM;

--
-- Table structure for table `map`
--

DROP TABLE IF EXISTS `map`;
CREATE TABLE `map` (
  `map_id` int(10) unsigned NOT NULL auto_increment,
  `map_name` varchar(30) NOT NULL default '',
  PRIMARY KEY  (`map_id`)
) TYPE=MyISAM;

--
-- Table structure for table `mapping_session`
--

DROP TABLE IF EXISTS `mapping_session`;
CREATE TABLE `mapping_session` (
  `mapping_session_id` int(11) NOT NULL auto_increment,
  `old_db_name` varchar(80) NOT NULL default '',
  `new_db_name` varchar(80) NOT NULL default '',
  `created` timestamp NOT NULL,
  PRIMARY KEY  (`mapping_session_id`)
) TYPE=MyISAM;

--
-- Table structure for table `marker`
--

DROP TABLE IF EXISTS `marker`;
CREATE TABLE `marker` (
  `marker_id` int(10) unsigned NOT NULL auto_increment,
  `display_marker_synonym_id` int(10) unsigned default NULL,
  `left_primer` varchar(100) NOT NULL default '',
  `right_primer` varchar(100) NOT NULL default '',
  `min_primer_dist` int(10) unsigned NOT NULL default '0',
  `max_primer_dist` int(10) unsigned NOT NULL default '0',
  `priority` int(11) default NULL,
  `type` enum('est','microsatellite') default NULL,
  PRIMARY KEY  (`marker_id`),
  KEY `marker_idx` (`marker_id`,`priority`)
) TYPE=MyISAM;

--
-- Table structure for table `marker_feature`
--

DROP TABLE IF EXISTS `marker_feature`;
CREATE TABLE `marker_feature` (
  `marker_feature_id` int(10) unsigned NOT NULL auto_increment,
  `marker_id` int(10) unsigned NOT NULL default '0',
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(10) unsigned NOT NULL default '0',
  `seq_region_end` int(10) unsigned NOT NULL default '0',
  `analysis_id` int(10) unsigned NOT NULL default '0',
  `map_weight` int(10) unsigned default NULL,
  PRIMARY KEY  (`marker_feature_id`),
  KEY `seq_region_idx` (`seq_region_id`,`seq_region_start`),
  KEY `analysis_idx` (`analysis_id`)
) TYPE=MyISAM;

--
-- Table structure for table `marker_map_location`
--

DROP TABLE IF EXISTS `marker_map_location`;
CREATE TABLE `marker_map_location` (
  `marker_id` int(10) unsigned NOT NULL default '0',
  `map_id` int(10) unsigned NOT NULL default '0',
  `chromosome_name` varchar(15) NOT NULL default '',
  `marker_synonym_id` int(10) unsigned NOT NULL default '0',
  `position` varchar(15) NOT NULL default '',
  `lod_score` double default NULL,
  PRIMARY KEY  (`marker_id`,`map_id`),
  KEY `map_idx` (`map_id`,`chromosome_name`,`position`)
) TYPE=MyISAM;

--
-- Table structure for table `marker_synonym`
--

DROP TABLE IF EXISTS `marker_synonym`;
CREATE TABLE `marker_synonym` (
  `marker_synonym_id` int(10) unsigned NOT NULL auto_increment,
  `marker_id` int(10) unsigned NOT NULL default '0',
  `source` varchar(20) default NULL,
  `name` varchar(30) default NULL,
  PRIMARY KEY  (`marker_synonym_id`),
  KEY `marker_synonym_idx` (`marker_synonym_id`,`name`),
  KEY `marker_idx` (`marker_id`)
) TYPE=MyISAM;

--
-- Table structure for table `meta`
--

DROP TABLE IF EXISTS `meta`;
CREATE TABLE `meta` (
  `meta_id` int(11) NOT NULL auto_increment,
  `meta_key` varchar(40) NOT NULL default '',
  `meta_value` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`meta_id`),
  KEY `meta_key_index` (`meta_key`),
  KEY `meta_value_index` (`meta_value`)
) TYPE=MyISAM;

--
-- Table structure for table `meta_coord`
--

DROP TABLE IF EXISTS `meta_coord`;
CREATE TABLE `meta_coord` (
  `table_name` varchar(40) NOT NULL default '',
  `coord_system_id` int(11) NOT NULL default '0',
  `max_length` int(11) default NULL,
  UNIQUE KEY `table_name` (`table_name`,`coord_system_id`)
) TYPE=MyISAM;

--
-- Table structure for table `misc_attrib`
--

DROP TABLE IF EXISTS `misc_attrib`;
CREATE TABLE `misc_attrib` (
  `misc_feature_id` int(10) unsigned NOT NULL default '0',
  `attrib_type_id` smallint(5) unsigned NOT NULL default '0',
  `value` varchar(255) NOT NULL default '',
  KEY `type_val_idx` (`attrib_type_id`,`value`),
  KEY `misc_feature_idx` (`misc_feature_id`)
) TYPE=MyISAM;

--
-- Table structure for table `misc_feature`
--

DROP TABLE IF EXISTS `misc_feature`;
CREATE TABLE `misc_feature` (
  `misc_feature_id` int(10) unsigned NOT NULL auto_increment,
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(10) unsigned NOT NULL default '0',
  `seq_region_end` int(10) unsigned NOT NULL default '0',
  `seq_region_strand` tinyint(4) NOT NULL default '0',
  PRIMARY KEY  (`misc_feature_id`),
  KEY `seq_region_idx` (`seq_region_id`,`seq_region_start`)
) TYPE=MyISAM;

--
-- Table structure for table `misc_feature_misc_set`
--

DROP TABLE IF EXISTS `misc_feature_misc_set`;
CREATE TABLE `misc_feature_misc_set` (
  `misc_feature_id` int(10) unsigned NOT NULL default '0',
  `misc_set_id` smallint(5) unsigned NOT NULL default '0',
  PRIMARY KEY  (`misc_feature_id`,`misc_set_id`),
  KEY `reverse_idx` (`misc_set_id`,`misc_feature_id`)
) TYPE=MyISAM;

--
-- Table structure for table `misc_set`
--

DROP TABLE IF EXISTS `misc_set`;
CREATE TABLE `misc_set` (
  `misc_set_id` smallint(5) unsigned NOT NULL auto_increment,
  `code` varchar(25) NOT NULL default '',
  `name` varchar(255) NOT NULL default '',
  `description` text NOT NULL,
  `max_length` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`misc_set_id`),
  UNIQUE KEY `c` (`code`)
) TYPE=MyISAM;

--
-- Table structure for table `object_xref`
--

DROP TABLE IF EXISTS `object_xref`;
CREATE TABLE `object_xref` (
  `object_xref_id` int(11) NOT NULL auto_increment,
  `ensembl_id` int(10) unsigned NOT NULL default '0',
  `ensembl_object_type` enum('RawContig','Transcript','Gene','Translation','regulatory_factor','regulatory_feature') NOT NULL default 'RawContig',
  `xref_id` int(10) unsigned NOT NULL default '0',
  UNIQUE KEY `ensembl_object_type` (`ensembl_object_type`,`ensembl_id`,`xref_id`),
  KEY `oxref_idx` (`object_xref_id`,`xref_id`,`ensembl_object_type`,`ensembl_id`),
  KEY `xref_idx` (`xref_id`,`ensembl_object_type`)
) TYPE=MyISAM;

--
-- Table structure for table `peptide_archive`
--

DROP TABLE IF EXISTS `peptide_archive`;
CREATE TABLE `peptide_archive` (
  `peptide_archive_id` int(11) NOT NULL auto_increment,
  `md5_checksum` varchar(32) default NULL,
  `peptide_seq` mediumtext NOT NULL,
  PRIMARY KEY  (`peptide_archive_id`),
  KEY `checksum` (`md5_checksum`)
) TYPE=MyISAM;

--
-- Table structure for table `prediction_exon`
--

DROP TABLE IF EXISTS `prediction_exon`;
CREATE TABLE `prediction_exon` (
  `prediction_exon_id` int(10) unsigned NOT NULL auto_increment,
  `prediction_transcript_id` int(10) unsigned NOT NULL default '0',
  `exon_rank` smallint(5) unsigned NOT NULL default '0',
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(10) unsigned NOT NULL default '0',
  `seq_region_end` int(10) unsigned NOT NULL default '0',
  `seq_region_strand` tinyint(4) NOT NULL default '0',
  `start_phase` tinyint(4) NOT NULL default '0',
  `score` double default NULL,
  `p_value` double default NULL,
  PRIMARY KEY  (`prediction_exon_id`),
  KEY `prediction_transcript_id` (`prediction_transcript_id`),
  KEY `seq_region_id` (`seq_region_id`,`seq_region_start`)
) TYPE=MyISAM;

--
-- Table structure for table `prediction_transcript`
--

DROP TABLE IF EXISTS `prediction_transcript`;
CREATE TABLE `prediction_transcript` (
  `prediction_transcript_id` int(10) unsigned NOT NULL auto_increment,
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(10) unsigned NOT NULL default '0',
  `seq_region_end` int(10) unsigned NOT NULL default '0',
  `seq_region_strand` tinyint(4) NOT NULL default '0',
  `analysis_id` int(11) default NULL,
  `display_label` varchar(255) default NULL,
  PRIMARY KEY  (`prediction_transcript_id`),
  KEY `seq_region_id` (`seq_region_id`,`seq_region_start`),
  KEY `analysis_idx` (`analysis_id`)
) TYPE=MyISAM;

--
-- Table structure for table `protein_align_feature`
--

DROP TABLE IF EXISTS `protein_align_feature`;
CREATE TABLE `protein_align_feature` (
  `protein_align_feature_id` int(10) unsigned NOT NULL auto_increment,
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(10) unsigned NOT NULL default '0',
  `seq_region_end` int(10) unsigned NOT NULL default '0',
  `seq_region_strand` tinyint(1) NOT NULL default '1',
  `hit_start` int(10) NOT NULL default '0',
  `hit_end` int(10) NOT NULL default '0',
  `hit_name` varchar(40) NOT NULL default '',
  `analysis_id` int(10) unsigned NOT NULL default '0',
  `score` double default NULL,
  `evalue` double default NULL,
  `perc_ident` float default NULL,
  `cigar_line` text,
  PRIMARY KEY  (`protein_align_feature_id`),
  KEY `analysis_idx` (`analysis_id`),
  KEY `hit_idx` (`hit_name`),
  KEY `seq_region_idx_2` (`seq_region_id`,`seq_region_start`),
  KEY `seq_region_idx` (`seq_region_id`,`analysis_id`,`seq_region_start`,`score`)
) TYPE=MyISAM MAX_ROWS=100000000 AVG_ROW_LENGTH=80;

--
-- Table structure for table `protein_feature`
--

DROP TABLE IF EXISTS `protein_feature`;
CREATE TABLE `protein_feature` (
  `protein_feature_id` int(10) unsigned NOT NULL auto_increment,
  `translation_id` int(11) NOT NULL default '0',
  `seq_start` int(10) NOT NULL default '0',
  `seq_end` int(10) NOT NULL default '0',
  `hit_start` int(10) NOT NULL default '0',
  `hit_end` int(10) NOT NULL default '0',
  `hit_id` varchar(40) NOT NULL default '',
  `analysis_id` int(10) unsigned NOT NULL default '0',
  `score` double NOT NULL default '0',
  `evalue` double default NULL,
  `perc_ident` float default NULL,
  PRIMARY KEY  (`protein_feature_id`),
  KEY `translation_id` (`translation_id`),
  KEY `hid_index` (`hit_id`),
  KEY `analysis_idx` (`analysis_id`)
) TYPE=MyISAM;

--
-- Table structure for table `qtl`
--

DROP TABLE IF EXISTS `qtl`;
CREATE TABLE `qtl` (
  `qtl_id` int(10) unsigned NOT NULL auto_increment,
  `trait` varchar(255) NOT NULL default '',
  `lod_score` float default NULL,
  `flank_marker_id_1` int(11) default NULL,
  `flank_marker_id_2` int(11) default NULL,
  `peak_marker_id` int(11) default NULL,
  PRIMARY KEY  (`qtl_id`),
  KEY `trait_idx` (`trait`)
) TYPE=MyISAM;

--
-- Table structure for table `qtl_feature`
--

DROP TABLE IF EXISTS `qtl_feature`;
CREATE TABLE `qtl_feature` (
  `seq_region_id` int(11) NOT NULL default '0',
  `seq_region_start` int(11) NOT NULL default '0',
  `seq_region_end` int(11) NOT NULL default '0',
  `qtl_id` int(11) NOT NULL default '0',
  `analysis_id` int(11) NOT NULL default '0',
  KEY `qtl_id` (`qtl_id`),
  KEY `loc_idx` (`seq_region_id`,`seq_region_start`),
  KEY `analysis_idx` (`analysis_id`)
) TYPE=MyISAM;

--
-- Table structure for table `qtl_synonym`
--

DROP TABLE IF EXISTS `qtl_synonym`;
CREATE TABLE `qtl_synonym` (
  `qtl_synonym_id` int(10) unsigned NOT NULL auto_increment,
  `qtl_id` int(10) unsigned NOT NULL default '0',
  `source_database` enum('rat genome database','ratmap') NOT NULL default 'rat genome database',
  `source_primary_id` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`qtl_synonym_id`),
  KEY `qtl_idx` (`qtl_id`)
) TYPE=MyISAM;

--
-- Table structure for table `regulatory_factor`
--

DROP TABLE IF EXISTS `regulatory_factor`;
CREATE TABLE `regulatory_factor` (
  `regulatory_factor_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `type` enum('miRNA_target','transcription_factor','transcription_factor_complex') default NULL,
  PRIMARY KEY  (`regulatory_factor_id`)
) TYPE=MyISAM;

--
-- Table structure for table `regulatory_factor_coding`
--

DROP TABLE IF EXISTS `regulatory_factor_coding`;
CREATE TABLE `regulatory_factor_coding` (
  `regulatory_factor_id` int(11) NOT NULL default '0',
  `transcript_id` int(11) default NULL,
  `gene_id` int(11) default NULL,
  KEY `transcript_idx` (`transcript_id`),
  KEY `gene_idx` (`gene_id`),
  KEY `regulatory_factor_idx` (`regulatory_factor_id`)
) TYPE=MyISAM;

--
-- Table structure for table `regulatory_feature`
--

DROP TABLE IF EXISTS `regulatory_feature`;
CREATE TABLE `regulatory_feature` (
  `regulatory_feature_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `seq_region_id` int(11) NOT NULL default '0',
  `seq_region_start` int(11) NOT NULL default '0',
  `seq_region_end` int(11) NOT NULL default '0',
  `seq_region_strand` tinyint(4) NOT NULL default '0',
  `analysis_id` int(11) NOT NULL default '0',
  `regulatory_factor_id` int(11) default NULL,
  PRIMARY KEY  (`regulatory_feature_id`),
  KEY `seq_region_idx` (`seq_region_id`,`analysis_id`,`seq_region_start`),
  KEY `seq_region_idx_2` (`seq_region_id`,`seq_region_start`)
) TYPE=MyISAM;

--
-- Table structure for table `regulatory_feature_object`
--

DROP TABLE IF EXISTS `regulatory_feature_object`;
CREATE TABLE `regulatory_feature_object` (
  `regulatory_feature_id` int(11) NOT NULL default '0',
  `ensembl_object_type` enum('Transcript','Translation','Gene') NOT NULL default 'Transcript',
  `ensembl_object_id` int(11) NOT NULL default '0',
  `influence` enum('positive','negative','mixed','unknown') default NULL,
  `evidence` varchar(255) default NULL,
  KEY `regulatory_feature_idx` (`regulatory_feature_id`),
  KEY `ensembl_object_idx` (`ensembl_object_type`,`ensembl_object_id`)
) TYPE=MyISAM;

--
-- Table structure for table `regulatory_search_region`
--

DROP TABLE IF EXISTS `regulatory_search_region`;
CREATE TABLE `regulatory_search_region` (
  `regulatory_search_region_id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL default '',
  `seq_region_id` int(11) NOT NULL default '0',
  `seq_region_start` int(11) NOT NULL default '0',
  `seq_region_end` int(11) NOT NULL default '0',
  `seq_region_strand` tinyint(4) NOT NULL default '0',
  `ensembl_object_type` enum('Transcript','Translation','Gene') NOT NULL default 'Transcript',
  `ensembl_object_id` int(11) default NULL,
  `analysis_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`regulatory_search_region_id`),
  KEY `rsr_idx` (`regulatory_search_region_id`),
  KEY `ensembl_object_idx` (`ensembl_object_type`,`ensembl_object_id`),
  KEY `seq_region_idx` (`seq_region_id`,`seq_region_start`),
  KEY `seq_region_idx_2` (`seq_region_id`,`seq_region_start`)
) TYPE=MyISAM;

--
-- Table structure for table `repeat_consensus`
--

DROP TABLE IF EXISTS `repeat_consensus`;
CREATE TABLE `repeat_consensus` (
  `repeat_consensus_id` int(10) unsigned NOT NULL auto_increment,
  `repeat_name` varchar(255) NOT NULL default '',
  `repeat_class` varchar(100) NOT NULL default '',
  `repeat_type` varchar(40) NOT NULL default '',
  `repeat_consensus` text,
  PRIMARY KEY  (`repeat_consensus_id`),
  KEY `name` (`repeat_name`),
  KEY `class` (`repeat_class`),
  KEY `consensus` (`repeat_consensus`(10)),
  KEY `type` (`repeat_type`)
) TYPE=MyISAM;

--
-- Table structure for table `repeat_feature`
--

DROP TABLE IF EXISTS `repeat_feature`;
CREATE TABLE `repeat_feature` (
  `repeat_feature_id` int(10) unsigned NOT NULL auto_increment,
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(10) unsigned NOT NULL default '0',
  `seq_region_end` int(10) unsigned NOT NULL default '0',
  `seq_region_strand` tinyint(1) NOT NULL default '1',
  `repeat_start` int(10) NOT NULL default '0',
  `repeat_end` int(10) NOT NULL default '0',
  `repeat_consensus_id` int(10) unsigned NOT NULL default '0',
  `analysis_id` int(10) unsigned NOT NULL default '0',
  `score` double default NULL,
  PRIMARY KEY  (`repeat_feature_id`),
  KEY `seq_region_idx` (`seq_region_id`,`seq_region_start`),
  KEY `repeat_idx` (`repeat_consensus_id`),
  KEY `analysis_idx` (`analysis_id`)
) TYPE=MyISAM MAX_ROWS=100000000 AVG_ROW_LENGTH=80;

--
-- Table structure for table `seq_region`
--

DROP TABLE IF EXISTS `seq_region`;
CREATE TABLE `seq_region` (
  `seq_region_id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(40) NOT NULL default '',
  `coord_system_id` int(10) NOT NULL default '0',
  `length` int(10) NOT NULL default '0',
  PRIMARY KEY  (`seq_region_id`),
  UNIQUE KEY `coord_system_id` (`coord_system_id`,`name`),
  KEY `name_idx` (`name`)
) TYPE=MyISAM;

--
-- Table structure for table `seq_region_attrib`
--

DROP TABLE IF EXISTS `seq_region_attrib`;
CREATE TABLE `seq_region_attrib` (
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `attrib_type_id` smallint(5) unsigned NOT NULL default '0',
  `value` varchar(255) NOT NULL default '',
  KEY `type_val_idx` (`attrib_type_id`,`value`),
  KEY `seq_region_idx` (`seq_region_id`)
) TYPE=MyISAM;

--
-- Table structure for table `simple_feature`
--

DROP TABLE IF EXISTS `simple_feature`;
CREATE TABLE `simple_feature` (
  `simple_feature_id` int(10) unsigned NOT NULL auto_increment,
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(10) unsigned NOT NULL default '0',
  `seq_region_end` int(10) unsigned NOT NULL default '0',
  `seq_region_strand` tinyint(1) NOT NULL default '0',
  `display_label` varchar(40) NOT NULL default '',
  `analysis_id` int(10) unsigned NOT NULL default '0',
  `score` double default NULL,
  PRIMARY KEY  (`simple_feature_id`),
  KEY `seq_region_idx` (`seq_region_id`,`seq_region_start`),
  KEY `analysis_idx` (`analysis_id`),
  KEY `hit_idx` (`display_label`)
) TYPE=MyISAM MAX_ROWS=100000000 AVG_ROW_LENGTH=80;

--
-- Table structure for table `stable_id_event`
--

DROP TABLE IF EXISTS `stable_id_event`;
CREATE TABLE `stable_id_event` (
  `old_stable_id` varchar(128) default NULL,
  `old_version` smallint(6) default NULL,
  `new_stable_id` varchar(128) default NULL,
  `new_version` smallint(6) default NULL,
  `mapping_session_id` int(11) NOT NULL default '0',
  `type` enum('gene','transcript','translation') NOT NULL default 'gene',
  UNIQUE KEY `uni_idx` (`mapping_session_id`,`old_stable_id`,`old_version`,`new_stable_id`,`new_version`,`type`),
  KEY `new_idx` (`new_stable_id`),
  KEY `old_idx` (`old_stable_id`)
) TYPE=MyISAM;

--
-- Table structure for table `supporting_feature`
--

DROP TABLE IF EXISTS `supporting_feature`;
CREATE TABLE `supporting_feature` (
  `exon_id` int(11) NOT NULL default '0',
  `feature_type` enum('dna_align_feature','protein_align_feature') default NULL,
  `feature_id` int(11) NOT NULL default '0',
  UNIQUE KEY `all_idx` (`exon_id`,`feature_type`,`feature_id`),
  KEY `feature_idx` (`feature_type`,`feature_id`)
) TYPE=MyISAM MAX_ROWS=100000000 AVG_ROW_LENGTH=80;

--
-- Table structure for table `transcript`
--

DROP TABLE IF EXISTS `transcript`;
CREATE TABLE `transcript` (
  `transcript_id` int(10) unsigned NOT NULL auto_increment,
  `gene_id` int(10) unsigned NOT NULL default '0',
  `seq_region_id` int(10) unsigned NOT NULL default '0',
  `seq_region_start` int(10) unsigned NOT NULL default '0',
  `seq_region_end` int(10) unsigned NOT NULL default '0',
  `seq_region_strand` tinyint(2) NOT NULL default '0',
  `display_xref_id` int(10) unsigned default NULL,
  `biotype` varchar(40) NOT NULL default 'protein_coding',
  `status` enum('KNOWN','NOVEL','PUTATIVE','PREDICTED') default NULL,
  `description` text,
  PRIMARY KEY  (`transcript_id`),
  KEY `seq_region_idx` (`seq_region_id`,`seq_region_start`),
  KEY `gene_index` (`gene_id`),
  KEY `xref_id_index` (`display_xref_id`)
) TYPE=MyISAM;

--
-- Table structure for table `transcript_attrib`
--

DROP TABLE IF EXISTS `transcript_attrib`;
CREATE TABLE `transcript_attrib` (
  `transcript_id` int(10) unsigned NOT NULL default '0',
  `attrib_type_id` smallint(5) unsigned NOT NULL default '0',
  `value` varchar(255) NOT NULL default '',
  KEY `type_val_idx` (`attrib_type_id`,`value`),
  KEY `transcript_idx` (`transcript_id`)
) TYPE=MyISAM;

--
-- Table structure for table `transcript_stable_id`
--

DROP TABLE IF EXISTS `transcript_stable_id`;
CREATE TABLE `transcript_stable_id` (
  `transcript_id` int(10) unsigned NOT NULL default '0',
  `stable_id` varchar(128) NOT NULL default '',
  `version` int(10) default NULL,
  `created_date` datetime NOT NULL default '0000-00-00 00:00:00',
  `modified_date` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`transcript_id`),
  UNIQUE KEY `stable_id` (`stable_id`,`version`)
) TYPE=MyISAM;

--
-- Table structure for table `transcript_supporting_feature`
--

DROP TABLE IF EXISTS `transcript_supporting_feature`;
CREATE TABLE `transcript_supporting_feature` (
  `transcript_id` int(11) NOT NULL default '0',
  `feature_type` enum('dna_align_feature','protein_align_feature') default NULL,
  `feature_id` int(11) NOT NULL default '0',
  UNIQUE KEY `all_idx` (`transcript_id`,`feature_type`,`feature_id`),
  KEY `feature_idx` (`feature_type`,`feature_id`)
) TYPE=MyISAM MAX_ROWS=100000000 AVG_ROW_LENGTH=80;

--
-- Table structure for table `translation`
--

DROP TABLE IF EXISTS `translation`;
CREATE TABLE `translation` (
  `translation_id` int(10) unsigned NOT NULL auto_increment,
  `transcript_id` int(10) unsigned NOT NULL default '0',
  `seq_start` int(10) NOT NULL default '0',
  `start_exon_id` int(10) unsigned NOT NULL default '0',
  `seq_end` int(10) NOT NULL default '0',
  `end_exon_id` int(10) unsigned NOT NULL default '0',
  PRIMARY KEY  (`translation_id`),
  KEY `transcript_id` (`transcript_id`)
) TYPE=MyISAM;

--
-- Table structure for table `translation_attrib`
--

DROP TABLE IF EXISTS `translation_attrib`;
CREATE TABLE `translation_attrib` (
  `translation_id` int(10) unsigned NOT NULL default '0',
  `attrib_type_id` smallint(5) unsigned NOT NULL default '0',
  `value` varchar(255) NOT NULL default '',
  KEY `type_val_idx` (`attrib_type_id`,`value`),
  KEY `translation_idx` (`translation_id`)
) TYPE=MyISAM;

--
-- Table structure for table `translation_stable_id`
--

DROP TABLE IF EXISTS `translation_stable_id`;
CREATE TABLE `translation_stable_id` (
  `translation_id` int(10) unsigned NOT NULL default '0',
  `stable_id` varchar(128) NOT NULL default '',
  `version` int(10) default NULL,
  `created_date` datetime NOT NULL default '0000-00-00 00:00:00',
  `modified_date` datetime NOT NULL default '0000-00-00 00:00:00',
  PRIMARY KEY  (`translation_id`),
  UNIQUE KEY `stable_id` (`stable_id`,`version`)
) TYPE=MyISAM;

--
-- Table structure for table `xref`
--

DROP TABLE IF EXISTS `xref`;
CREATE TABLE `xref` (
  `xref_id` int(10) unsigned NOT NULL auto_increment,
  `external_db_id` int(11) NOT NULL default '0',
  `dbprimary_acc` varchar(40) NOT NULL default '',
  `display_label` varchar(128) NOT NULL default '',
  `version` varchar(10) NOT NULL default '',
  `description` varchar(255) default NULL,
  PRIMARY KEY  (`xref_id`),
  UNIQUE KEY `id_index` (`dbprimary_acc`,`external_db_id`),
  KEY `display_index` (`display_label`),
  KEY `T` (`external_db_id`,`display_label`)
) TYPE=MyISAM;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

