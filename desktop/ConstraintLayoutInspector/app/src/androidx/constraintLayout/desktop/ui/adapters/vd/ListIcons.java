/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.constraintLayout.desktop.ui.adapters.vd;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ListIcons {
  private static final String SUFFIX = ".xml";
  String[] results;

  public static URL[] getSVGs() {
    ListIcons l = new ListIcons();
    URL[] u = new URL[l.results.length];
    for (int i = 0; i < u.length; i++) {
      u[i] = ListIcons.class.getResource(l.results[i]);
    }
    return u;
  }

  public static InputStream getStream(String str) {
    try {
      URL u = ListIcons.class.getResource(
          ListIcons.class.getSimpleName() + ".class");
      if (isFileSystem(u)) {
        String urlPath = u.toString();
        String basePath = urlPath.substring(0, urlPath.lastIndexOf("/"));
        File dir = new File(u.getFile()).getParentFile();
        File f = new File(dir, str);
        return new File(dir, str).toURI().toURL().openStream();
      }
      return ListIcons.class.getResourceAsStream(str);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static URL getURL(String str) {
    try {
      URL u = ListIcons.class.getResource(
          ListIcons.class.getSimpleName() + ".class");
      if (isFileSystem(u)) {
        String urlPath = u.toString();
        String basePath = urlPath.substring(0, urlPath.lastIndexOf("/"));
        File dir = new File(u.getFile()).getParentFile();
        File f = new File(dir, str);
        return new File(dir, str).toURI().toURL();
      } else {

        URLConnection conn = u.openConnection();

        if (conn instanceof JarURLConnection) {
          JarURLConnection connection = (JarURLConnection) conn;
          URL j = connection.getJarFileURL();
          JarFile f = new JarFile(j.getPath());
          String fullname = ListIcons.class.getName();
          fullname = fullname.substring(0, fullname.lastIndexOf(ListIcons.class.getSimpleName()));
          System.out.println("  fullname " + fullname);
          fullname += "/" + str;
          ZipEntry entry = f.getEntry(fullname);

          return new URL(j, fullname);
        }

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static boolean isFileSystem(URL u) {
    String urlPath = u.toString();

    return (urlPath.startsWith("file"));
  }

  public ListIcons() {

    URL u = ListIcons.class.getResource(this.getClass().getSimpleName() + ".class");
    String urlPath = u.toString();
    String basePath = urlPath.substring(0, urlPath.lastIndexOf("/"));
    if (urlPath.startsWith("file")) {
      URL bu;
      try {
        bu = new URL(basePath);
        File f = new File(bu.getFile());
        final File[] list = f.listFiles(new FileFilter() {

          @Override
          public boolean accept(File pathname) {

            return pathname.toString().endsWith(SUFFIX);
          }
        });
        results = new String[list.length];
        for (int j = 0; j < list.length; j++) {
          results[j] = list[j].getName();
        }
      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    } else { // in jar

      URL bu;
      try {
        URLConnection conn = u.openConnection();
        if (conn instanceof JarURLConnection) {
          JarURLConnection connection = (JarURLConnection) conn;
          URL j = connection.getJarFileURL();
          JarFile f = new JarFile(j.getPath());

          String fullname = ListIcons.class.getName();

          fullname = fullname.substring(0, fullname.lastIndexOf(ListIcons.class.getSimpleName()));
          fullname = fullname.replaceAll("\\.", "/");

          Enumeration<JarEntry> e = f.entries();
          ArrayList<String> al = new ArrayList<String>();
          while (e.hasMoreElements()) {
            ZipEntry ze = (ZipEntry) e.nextElement();
            String name = ze.getName();
            if (name.endsWith(SUFFIX)) {
              al.add(name.substring(name.lastIndexOf("/") + 1));
            }

          }
          results = al.toArray(new String[al.size()]);
        }

      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

}

