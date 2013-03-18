// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2013 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.pathvisio.pluginmanager.impl.io;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.pathvisio.pluginmanager.impl.Utils;
import org.pathvisio.pluginmanager.impl.data.Affiliation;
import org.pathvisio.pluginmanager.impl.data.BundleAuthor;
import org.pathvisio.pluginmanager.impl.data.BundleVersion;
import org.pathvisio.pluginmanager.impl.data.Category;
import org.pathvisio.pluginmanager.impl.data.Developer;
import org.pathvisio.pluginmanager.impl.data.PVBundle;
import org.pathvisio.pluginmanager.impl.data.PVRepository;
import org.pathvisio.pluginmanager.impl.data.Profile;

public class RepoXmlReader {
	
	private static String SYMBOLIC_NAME = "symbolic_name";
	private static String TYPE = "type";
	private static String NAME = "name";
	private static String BUNDLE_ID = "bundle_id";
	
	private Map<String, PVBundle> bundleMap;
	private Map<String, BundleVersion> bundleVersionMap;
	private Map<String, Affiliation> affiliationMap;
	private Map<String, org.pathvisio.pluginmanager.impl.data.PVRepository> repoMap;
	private Map<String, Category> categories;
	private Map<String, Developer> developerMap;
	private Map<String, Profile> profileMap;
	
	public RepoXmlReader() {
		bundleMap = new HashMap<String, PVBundle>();
		bundleVersionMap = new HashMap<String, BundleVersion>();
		affiliationMap = new HashMap<String, Affiliation>();
		repoMap = new HashMap<String, org.pathvisio.pluginmanager.impl.data.PVRepository>();
		categories = new HashMap<String, Category>();
		developerMap = new HashMap<String, Developer>();
		profileMap = new HashMap<String, Profile>();
	}
	
	public List<PVRepository> parseFile(URL url) {
		
		try {
			URL pathVisioXml = Utils.getXMLURL(url);
			SAXBuilder parser = new SAXBuilder();
			Document doc = parser.build(pathVisioXml);
			Element root = doc.getRootElement();
			Element db = root.getChild("database");
			if(db != null) {
				List<Element> list = db.getChildren("table_data");
				
				for(Element e : list) {
					String table = e.getAttributeValue("name");
					
					// read repo data
					if(table.equals("repository")) {
						List<Element> l = e.getChildren("row");
						for(Element row : l) {
							readRepository(row);
						}
					}
					// read bundle data
					else if(table.equals("bundle")) {
						List<Element> l = e.getChildren("row");
						for(Element row : l) {
							readBundleData(row);
						}
					}
					// read affiliation
					else if (table.equals("affiliation")) {
						List<Element> l = e.getChildren("row");
						for(Element row : l) {
							readAffiliation(row);
						}
					}
					//read categories
					else if (table.equals("category")) {
						List<Element> l = e.getChildren("row");
						for(Element row : l) {
							readCategories(row);
						}
					}
					//read developers
					else if (table.equals("developer")) {
						List<Element> l = e.getChildren("row");
						for(Element row : l) {
							readDevelopers(row);
						}
					}
					// read bundle version
					else if (table.equals("bundle_version")) {
						List<Element> l = e.getChildren("row");
						for(Element row : l) {
							readBundleVersionData(row);
						}
					} 
					// read bundle categories
					else if (table.equals("bundle_categories")) {
						List<Element> l = e.getChildren("row");
						for(Element row : l) {
							addPluginCategories(row);
						}
					} 
					// read bundle authors
					else if (table.equals("bundle_version_author")) {
						List<Element> l = e.getChildren("row");
						for(Element row : l) {
							addPluginAuthors(row);
						}
					} 
					// read profile categories
					else if (table.equals("profile_categories")) {
						List<Element> l = e.getChildren("row");
						for(Element row : l) {
							addProfileCategories(row);
						}
					} 
					// read repository bundles
					else if (table.equals("repository_bundles")) {
						List<Element> l = e.getChildren("row");
						for(Element row : l) {
							addRepoBundles(row);
						}
					} 
				}
			}
			
			List<PVRepository> list = new ArrayList<PVRepository>();
			for(String key : repoMap.keySet()) {
				list.add(repoMap.get(key));
				for(BundleVersion version : repoMap.get(key).getBundleVersions()) {
					version.getBundle().setSource(repoMap.get(key).getUrl());
				}
			}

			cleanUp();
			return list;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void cleanUp() {
		bundleMap.clear();
		bundleVersionMap.clear();
		affiliationMap.clear();
		repoMap.clear();
		categories.clear();
		developerMap.clear();
		profileMap.clear();
	}

	private void addRepoBundles(Element element) {
		List<Element> fields = element.getChildren("field");
		
		String repositoryId = "";
		String bundleVersionId = "";
		
		for(Element f : fields) {
			String attribute = f.getAttributeValue("name");
			
			if(attribute.equals("repository_id")) {
				repositoryId = f.getValue();
			} else if(attribute.equals("bundle_version_id")) {
				bundleVersionId = f.getValue();
			}
		}
		
		if(repoMap.containsKey(repositoryId) && bundleVersionMap.containsKey(bundleVersionId)) {
			repoMap.get(repositoryId).addPluginVersion(bundleVersionMap.get(bundleVersionId));
		}
	}

	private void addProfileCategories(Element element) {
		List<Element> fields = element.getChildren("field");
		
		String profileId = "";
		String categoryId = "";
		
		for(Element f : fields) {
			String attribute = f.getAttributeValue("name");
			
			if(attribute.equals("profile_id")) {
				profileId = f.getValue();
			} else if(attribute.equals("category_id")) {
				categoryId = f.getValue();
			}
		}
		
		if(profileMap.containsKey(profileId) && categories.containsKey(categoryId)) {
			profileMap.get(profileId).getCategories().add(categories.get(categoryId));
		}
	}

	private void addPluginAuthors(Element element) {
		List<Element> fields = element.getChildren("field");
		
		String bundleVersionId = "";
		String developerId = "";
		String affiliationId = "";
		
		for(Element f : fields) {
			String attribute = f.getAttributeValue("name");
			
			if(attribute.equals("bundle_version_id")) {
				bundleVersionId = f.getValue();
			} else if(attribute.equals("developer_id")) {
				developerId = f.getValue();
			} else if(attribute.equals("affiliation_id")) {
				affiliationId = f.getValue();
			}
		}
		
		if(bundleVersionMap.containsKey(bundleVersionId) && 
				developerMap.containsKey(developerId) &&
				affiliationMap.containsKey(affiliationId)) {
			
			BundleVersion version = bundleVersionMap.get(bundleVersionId);
			BundleAuthor author = new BundleAuthor();
//			author.setVersion(version);
			author.setAffiliation(affiliationMap.get(affiliationId));
			author.setDeveloper(developerMap.get(developerId));
			
			version.getAuthors().add(author);
		}	
	}
	
	private void addPluginCategories(Element element) {
		List<Element> fields = element.getChildren("field");
		
		String bundleId = "";
		String categoryId = "";
		
		for(Element f : fields) {
			String attribute = f.getAttributeValue("name");
			
			if(attribute.equals("bundle_id")) {
				bundleId = f.getValue();
			} else if(attribute.equals("category_id")) {
				categoryId = f.getValue();
			}
		}
		
		if(bundleMap.containsKey(bundleId) && categories.containsKey(categoryId)) {
			bundleMap.get(bundleId).getCategories().add(categories.get(categoryId));
		}
	}
	
	private void readProfiles(Element element) {
		List<Element> fields = element.getChildren("field");
		String profileId = "";
		Profile profile = new Profile();
		
		for(Element f : fields) {
			String attribute = f.getAttributeValue("name");
			
			if(attribute.equals("profile_id")) {
				profileId = f.getValue();
			} else if(attribute.equals("name")) {
				profile.setName(f.getValue());
			}
		}
		
		if(!profileId.equals("") && profile.getName() != null) {
			profileMap.put(profileId, profile);
		}
	}
	
	private void readDevelopers(Element element) {
		List<Element> fields = element.getChildren("field");
		String developerId = "";
		Developer developer = new Developer();
		
		for(Element f : fields) {
			String attribute = f.getAttributeValue("name");
			
			if(attribute.equals("developer_id")) {
				developerId = f.getValue();
			} else if(attribute.equals("firstname")) {
				developer.setFirstName(f.getValue());
			} else if(attribute.equals("lastname")) {
				developer.setLastName(f.getValue());
			}
		}
		
		if(!developerId.equals("") && developer.getLastName() != null) {
			developerMap.put(developerId, developer);
		}
	}

	private void readCategories(Element element) {
		List<Element> fields = element.getChildren("field");
		String categoryId = "";
		Category cat = new Category();
		for(Element f : fields) {
			String attribute = f.getAttributeValue("name");
			
			if(attribute.equals("category_id")) {
				categoryId = f.getValue();
			} else if(attribute.equals("name")) {
				cat.setName(f.getValue());
			} 
		}
		
		if(cat.getName() != null && !categoryId.equals("")) {
			categories.put(categoryId, cat);
		}
	}
	
	private void readRepository(Element element) {
		List<Element> fields = element.getChildren("field");
		String repoId = "";
		org.pathvisio.pluginmanager.impl.data.PVRepository repo = new org.pathvisio.pluginmanager.impl.data.PVRepository();
		
		for(Element f : fields) {
			String attribute = f.getAttributeValue("name");
			
			if(attribute.equals("repository_id")) {
				repoId = f.getValue();
			} else if(attribute.equals("name")) {
				repo.setName(f.getValue());
			} else if (attribute.equals("url")) {
				repo.setUrl(f.getValue());
			}
		}
		
		if(repo.getName() != null && repoId != null) {
			repoMap.put(repoId, repo);
		}
	}
	
	private void readAffiliation(Element element) {
		List<Element> fields = element.getChildren("field");
		
		String affiliationId = "";
		Affiliation a = new Affiliation();
		
		for(Element f : fields) {
			String attribute = f.getAttributeValue("name");
			
			if(attribute.equals("affiliation_id")) {
				affiliationId = f.getValue();
			} else if(attribute.equals("name")) {
				a.setName(f.getValue());
			} else if (attribute.equals("website")) {
				a.setWebsite(f.getValue());
			}
		}
		
		if(a.getName() != null && affiliationId != null) {
			affiliationMap.put(affiliationId, a);
		}
	}

	private void readBundleVersionData(Element element) {
		List<Element> fields = element.getChildren("field");
		
		BundleVersion pluginVersion = new BundleVersion();
		String bundleId = "";
		String bundleVersionId = "";
		
		for(Element f : fields) {
			String attribute = f.getAttributeValue("name");
			if(attribute.equals("bundle_version_id")) {
				bundleVersionId = f.getValue();
			} else if(attribute.equals("version")) {
				pluginVersion.setVersion(f.getValue());
			} else if (attribute.equals("jar_file_url")) {
				pluginVersion.setJarFile(f.getValue());
			} else if(attribute.equals(BUNDLE_ID)) {
				bundleId = f.getValue();
			} else if(attribute.equals("release_notes")) {
				pluginVersion.setReleaseNotes(f.getValue());
			} else if(attribute.equals("release_date")) {
				pluginVersion.setReleaseDate(f.getValue());
			} else if(attribute.equals("license")) {
				pluginVersion.setLicense(f.getValue());
			} else if(attribute.equals("icon_url")) {
				pluginVersion.setIconUrl(f.getValue());
			}
		}
		if(bundleMap.containsKey(bundleId)) {
			PVBundle p = bundleMap.get(bundleId);
			pluginVersion.setBundle(p);
			bundleVersionMap.put(bundleVersionId, pluginVersion);
		} else {
			System.out.println("bundle with id " + bundleId + " not found. Version without bundle!");
		}
	}
	
	private void readBundleData(Element element) {
		List<Element> fields = element.getChildren("field");
		String symName = "";
		String name = "";
		String type = "";
		String description = "";
		String shortDescription = "";
		String website = "";
		String faq = "";
		String bundleId = "";
		for(Element f : fields) {
			String attribute = f.getAttributeValue("name");
			if (attribute.equals(BUNDLE_ID)) {
				bundleId = f.getValue();
			} else if (attribute.equals(NAME)) {
				name = f.getValue();
			} else if(attribute.equals(SYMBOLIC_NAME)) {
				if(f.getValue().equals("org.pathvisio.html")) {
					symName = "org.pathvisio.htmlexport";
				} else {
					symName = f.getValue();
				}
			} else if (attribute.equals(TYPE)) {
				type = f.getValue();
			} else if(attribute.equals("description")) {
				description = f.getValue();
			} else if(attribute.equals("faq")) {
				faq = f.getValue();
			} else if(attribute.equals("short_description")) {
				shortDescription = f.getValue();
			} else if(attribute.equals("website")) {
				website = f.getValue();
			}
		}
		if(type.equals("plugin") || type.equals("lib") || type.equals("core")) {
			PVBundle plugin = new PVBundle();
			plugin.setName(name);
			plugin.setSymbolicName(symName);
			plugin.setDescription(description);
			plugin.setFaq(faq);
			plugin.setShortDescription(shortDescription);
			plugin.setWebsite(website);
			plugin.setType(type);
			if(!bundleMap.containsKey(bundleId)) {
				bundleMap.put(bundleId, plugin);
			} else {
				System.out.println("Double bundle id?");
			}
		}
	}
}
