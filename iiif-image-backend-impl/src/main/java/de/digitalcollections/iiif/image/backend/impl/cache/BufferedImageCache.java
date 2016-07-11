package de.digitalcollections.iiif.image.backend.impl.cache;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LRU-Cache for buffered images using ImageIO read/write on filesystem. Least Recently Used (LRU): Strategy sorting
 * objects in cache according to last usage. If an element is not accessed a longer time it will be pushed out of cache:
 */
public class BufferedImageCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(BufferedImageCache.class);

  private int MAX_CAPACITY = 500;

  /**
   * The directory where we store our JPEG images
   */
  private String cacheDir;

  /**
   * @return directory of cached images
   */
  public String getCacheDir() {
    return cacheDir;
  }

  /**
   * The compression quality of this JPEG persistence engine
   */
  private float compressionQuality = 1.0f;

  /**
   * Our set of keys; these are in fact simply the filenames in our directory, but maintained in y for speed
   */
//    private final Set keys = new TreeSet();
//    Map<String, String> lruMap = (Map<String, String>) Collections.synchronizedMap(new LRUMap(MAX_CAPACITY) {
//    });
  private final Map<String, Object> internalMap;

  /**
   * Creates a new Cache that reads and writes between BufferedImages and JPEG files
   */
  public BufferedImageCache() {
    ImageIO.setUseCache(true);
    this.internalMap = (Map<String, Object>) Collections.
            synchronizedMap(new LinkedHashMap<String, Object>(MAX_CAPACITY + 1, .75F, true) {
              private static final long serialVersionUID = 1L;

              @Override
              protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
                final boolean remove = size() > MAX_CAPACITY;
                if (remove) {
                  String key = eldest.getKey();
                  LOGGER.info("removing least read image {}", key);
                  removeCachedImage(key);
                }
                return remove;
              }
            });
  }

  /**
   * Checks if an image is cached.
   *
   * @param identifier unique identifier for image
   * @return true if image is in cache
   */
  public boolean isCached(String identifier) {
    return getInternalMap().containsKey(identifier);
  }

  /**
   * Puts the specified object into the cache with the specified key
   *
   * @param key cache key
   * @param bi image to be cached
   * @throws PersistenceException if image can not be written to cache
   */
  public void put(String key, BufferedImage bi) throws PersistenceException {
    try {
      if (cacheDir != null) {
        String filename = this.cacheDir + key;

        // Get Writer and set compression
        Iterator iter = ImageIO.getImageWritersByFormatName("JPG");
        if (iter.hasNext()) {
          ImageWriter writer = (ImageWriter) iter.next();
          ImageWriteParam iwp = writer.getDefaultWriteParam();
          iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
          iwp.setCompressionQuality(this.compressionQuality);
          MemoryCacheImageOutputStream mos = new MemoryCacheImageOutputStream(new FileOutputStream(filename));
          writer.setOutput(mos);
          IIOImage image = new IIOImage(bi, null, null);
          writer.write(null, image, iwp);
        }

        // add key
        getInternalMap().put(key, "dummy");
      } else {
        getInternalMap().put(key, bi);
      }
    } catch (Exception e) {
      LOGGER.error("Can not write image to cache", e);
      throw new PersistenceException(e);
    }

  }

  /**
   * Retrieves the requested object from the cache
   *
   * @param key cache key
   * @return cached image
   * @throws PersistenceException if image can not be read from cache
   */
  public BufferedImage get(String key) throws PersistenceException {
    try {
      BufferedImage image;
      if (cacheDir != null) {
        String filename = this.cacheDir + key;
        image = ImageIO.read(new File(filename));
      } else {
        image = (BufferedImage) this.internalMap.get(key);
      }
      return image;
    } catch (Exception e) {
      LOGGER.error("Can not read image from cache", e);
      throw new PersistenceException(e);
    }
  }

  /**
   * @return a set of all cache keys
   */
  public Map<String, Object> getInternalMap() {
    // Load our keys from the filesystem
    if (this.internalMap.isEmpty()) {
      if (this.cacheDir != null) {
        File dir = new File(this.cacheDir);
        String[] filenames = dir.list();
        if (filenames != null) {
          for (String filename : filenames) {
            this.internalMap.put(filename, "dummy");
          }
        }
      }
    }
    // Return our keys
    return this.internalMap;
  }

  /**
   * @param cacheDir cache directory
   */
  public void setCacheDir(String cacheDir) {
    File f = new File(cacheDir);
    if (!f.exists()) {
      f.mkdirs();
    }
    final String absolutePath = f.getAbsolutePath() + File.separator;
    this.cacheDir = absolutePath;
  }

  /**
   * Sets PersistenceEngine specific properties ("cache-dir" and "compression-quality")
   *
   * @param name name of property
   * @param value value of property
   */
  public void setProperty(String name, String value) {
    if (name.equalsIgnoreCase("cache-dir")) {
      this.cacheDir = System.getProperty("user.dir") + File.separator + value + File.separator;
    } else if (name.equalsIgnoreCase("compression-quality")) {
      this.compressionQuality = Float.parseFloat(value);
    }
  }

  /**
   * Allows a PersistenceEngine to be initialized with a set of name/value pairs
   *
   * @param p set of properties (name, value)-pairs
   */
  public void init(Properties p) {
    String cacheDirValue = p.getProperty("cache-dir");
    if (cacheDirValue == null) {
      cacheDirValue = "cache";
    }
    this.cacheDir = System.getProperty("user.dir") + File.separator + cacheDirValue + File.separator;

    String compressionQualityValue = p.getProperty("compression-quality");
    if (compressionQualityValue != null) {
      this.compressionQuality = Float.parseFloat(compressionQualityValue);
    }
  }

  /**
   * Removes the specified object from the cache
   *
   * @param key cache key for object
   * @throws PersistenceException if object can not be removed from cache
   */
  public void remove(String key) throws PersistenceException {
    try {
      if (cacheDir != null) {
        removeCachedImage(key);
      }

      // remove key
      getInternalMap().remove(key);
    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }

  private void removeCachedImage(String key) {
    File f = new File(this.cacheDir + key);
    f.delete();
  }

  /**
   * Removes all objects from the cache
   *
   * @throws PersistenceException if not all objects can be removed from cache
   */
  public void removeAll() throws PersistenceException {
    try {
      if (cacheDir != null) {
        File dir = new File(this.cacheDir);
        File[] files = dir.listFiles();
        for (File file : files) {
          file.delete();
        }
      }
      // remove all keys
      this.internalMap.clear();
    } catch (Exception e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public String toString() {
    return "BufferedImageCache=JPEG, cacheDir=" + this.cacheDir + ", compressionQuality=" + this.compressionQuality;
  }
}
