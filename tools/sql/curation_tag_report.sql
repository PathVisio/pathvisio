#This statement produces a tab-delimited table: | Pathway ID | Latest revision | Tags on specific revisions | All tags |
#You can import this table into Excel and use filters, sorting and functions to identify targets for curation or inconsistencies.

mysql -u wikipathwaysuser -p -D wikipathways -e 'select page_title, page_latest, group_concat(concat(tag_name, ":", tag.revision) order by tag_name separator ", ") as rev_tags, group_concat(tag_name order by tag_name separator ", ") as all_tags from  tag left join page using(page_id) where tag_name like "Curation%" group by page_title;' > tag_report.tab
