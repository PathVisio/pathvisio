#!/bin/sh

cd /var/www/wikipathways/wpi/sitemap

# Set the permissions for createSiteMap.php to enable access over http
echo "Setting permissions of createSiteMap.php to 777"
chmod 777 createSiteMap.php

# Generate new sitemap
echo "Generating sitemap"
wget -O /var/www/wikipathways/sitemap.xml http://www.wikipathways.org/wpi/sitemap/createSiteMap.php

# Set the correct permissions for sitemap.xml
echo "Setting permissions of sitemap.xml to 777"
chmod 777 /var/www/wikipathways/sitemap.xml

# Restore permissions
echo "Restoring permissions of createSiteMap.php"
chmod 700 createSiteMap.php
