/*
 * FILE: jpeg_tran.c
 * AUTHOR: Christian Kaufhold
 *
 *
 */

#include <stdlib.h>
#include <jni.h>
#include <stdio.h>

#include "Epeg.h"


#define JPEG_INTERNALS

#include "jinclude.h"
#include "jpeglib.h"
#include "transupp.h"           /* My own external interface */
#include <ctype.h>              /* to declare isdigit() */

int wrapper(JNIEnv*, unsigned char*, int, jobject, jpeg_transform_info *);
void init_transformoptions(jpeg_transform_info*);


int* get_dimensions(unsigned char* img, int size, int* dimensions) {
  Epeg_Image *im = epeg_memory_open(img, size);
  epeg_size_get(im, dimensions, dimensions+1);
  epeg_close(im);
}

JNIEXPORT jint JNICALL
Java_org_mdz_jpegtran_Transformation_getWidth(JNIEnv *env
                                              , jclass cls
                                              , jbyteArray jbyteArrayIn) {
  jsize size = (*env)->GetArrayLength(env, jbyteArrayIn);
  jbyte* bufferIn = (*env)->GetByteArrayElements(env, jbyteArrayIn, NULL);

  int dimensions[2];
  get_dimensions(bufferIn, size, dimensions);
  (*env)->ReleaseByteArrayElements(env, jbyteArrayIn, (jbyte*) bufferIn, JNI_ABORT);
  return (jint) dimensions[0];
}

JNIEXPORT jint JNICALL
Java_org_mdz_jpegtran_Transformation_getHeight(JNIEnv *env
                                              , jclass cls
                                              , jbyteArray jbyteArrayIn) {
  jsize size = (*env)->GetArrayLength(env, jbyteArrayIn);
  jbyte* bufferIn = (*env)->GetByteArrayElements(env, jbyteArrayIn, NULL);

  int dimensions[2];
  get_dimensions(bufferIn, size, dimensions);
  (*env)->ReleaseByteArrayElements(env, jbyteArrayIn, (jbyte*) bufferIn, JNI_ABORT);
  return (jint) dimensions[1];
}

/*
 * downscale
 *
 *
 */
JNIEXPORT jint JNICALL
Java_org_mdz_jpegtran_Transformation_downscale(
        JNIEnv *env
        , jclass cls
        , jbyteArray jbyteArrayIn
        , jobject outObj
        , jint width
        , jint height
        , jint quality) {
  unsigned char* out_data;
  int out_size = 0;

  // to Java
  unsigned char* outbuf_p = (unsigned char*) (*env)->GetDirectBufferAddress(env, outObj);

  jsize size = (*env)->GetArrayLength(env, jbyteArrayIn);
  jbyte* bufferIn = (*env)->GetByteArrayElements(env, jbyteArrayIn, NULL);

  Epeg_Image *im = epeg_memory_open(bufferIn, size);
  epeg_decode_size_set(im, width, height);
  epeg_quality_set(im, quality);
  epeg_memory_output_set(im, &out_data, &out_size);
  epeg_encode(im);

  memcpy(outbuf_p, out_data, out_size);
  epeg_close(im);
  (*env)->ReleaseByteArrayElements(env, jbyteArrayIn, (jbyte*) bufferIn, JNI_ABORT);
  return (jint) out_size;
}

/*
 * crop
 *
 */
JNIEXPORT jint JNICALL
Java_org_mdz_jpegtran_Transformation_crop(
        JNIEnv *env
        , jclass cls
        , jbyteArray jbyteArrayIn
        , jobject outBuf
        , jint x
        , jint y
        , jint width
        , jint height) {
  jsize size = (*env)->GetArrayLength(env, jbyteArrayIn);
  jbyte* bufferIn = (*env)->GetByteArrayElements(env, jbyteArrayIn, NULL);

  jpeg_transform_info options;
  init_transformoptions(&options);
  options.perfect = FALSE;
  options.trim = FALSE;
  options.crop = TRUE;
  options.crop_width = width;
  options.crop_width_set = JCROP_FORCE;
  options.crop_height = height;
  options.crop_height_set = JCROP_FORCE;
  options.crop_xoffset = x;
  options.crop_xoffset_set = JCROP_POS;
  options.crop_yoffset = y;
  options.crop_yoffset_set = JCROP_POS;

  jint rv = wrapper(env, bufferIn, size, outBuf, &options);
  (*env)->ReleaseByteArrayElements(env, jbyteArrayIn, (jbyte*) bufferIn, JNI_ABORT);
  return rv;
}

/*
 * transpose
 *
 */
JNIEXPORT jint JNICALL
Java_org_mdz_jpegtran_Transformation_transpose(
        JNIEnv *env
        , jclass cls
        , jbyteArray jbyteArrayIn
        , jobject outBuf) {
  unsigned char *out_data;
  int out_size = 0;
  jsize size = (*env)->GetArrayLength(env, jbyteArrayIn);
  jbyte* bufferIn = (*env)->GetByteArrayElements(env, jbyteArrayIn, NULL);

  jpeg_transform_info options;
  init_transformoptions(&options);
  options.perfect = FALSE;
  options.trim = FALSE;
  options.transform = JXFORM_TRANSPOSE;

  jint rv = wrapper(env, bufferIn, size, outBuf, &options);
  (*env)->ReleaseByteArrayElements(env, jbyteArrayIn, (jbyte*) bufferIn, JNI_ABORT);
  return rv;
}

/*
 * transverse
 *
 */
JNIEXPORT jint JNICALL
Java_org_mdz_jpegtran_Transformation_transverse(
        JNIEnv *env
        , jclass cls
        , jbyteArray jbyteArrayIn
        , jobject outBuf) {
  unsigned char *out_data;
  int out_size = 0;
  jsize size = (*env)->GetArrayLength(env, jbyteArrayIn);
  jbyte* bufferIn = (*env)->GetByteArrayElements(env, jbyteArrayIn, NULL);

  jpeg_transform_info options;
  init_transformoptions(&options);
  options.perfect = FALSE;
  options.trim = FALSE;
  options.transform = JXFORM_TRANSVERSE;

  jint rv = wrapper(env, bufferIn, size, outBuf, &options);
  (*env)->ReleaseByteArrayElements(env, jbyteArrayIn, (jbyte*) bufferIn, JNI_ABORT);
  return rv;
}

/*
 * flip
 *
 */
JNIEXPORT jint JNICALL
Java_org_mdz_jpegtran_Transformation_flip(
        JNIEnv *env
        , jclass cls
        , jbyteArray jbyteArrayIn
        , jobject outBuf
        , jboolean vertical) {
  unsigned char *out_data;
  int out_size = 0;
  jsize size = (*env)->GetArrayLength(env, jbyteArrayIn);
  jbyte* bufferIn = (*env)->GetByteArrayElements(env, jbyteArrayIn, NULL);

  jpeg_transform_info options;
  init_transformoptions(&options);
  options.perfect = FALSE;
  options.trim = FALSE;
  if (vertical) {
    options.transform = JXFORM_FLIP_V;
  } else {
    options.transform = JXFORM_FLIP_H;
  }

  jint rv = wrapper(env, bufferIn, size, outBuf, &options);
  (*env)->ReleaseByteArrayElements(env, jbyteArrayIn, (jbyte*) bufferIn, JNI_ABORT);
  return rv;
}

/*
 * flip
 *
 */
JNIEXPORT jint JNICALL
Java_org_mdz_jpegtran_Transformation_rotate(
        JNIEnv *env
        , jclass cls
        , jbyteArray jbyteArrayIn
        , jobject outBuf
        , jint angle) {
  unsigned char *out_data;
  int out_size = 0;
  jsize size = (*env)->GetArrayLength(env, jbyteArrayIn);
  jbyte* bufferIn = (*env)->GetByteArrayElements(env, jbyteArrayIn, NULL);

  jpeg_transform_info options;
  init_transformoptions(&options);
  options.perfect = FALSE;
  options.trim = FALSE;
  if (angle == 90) {
    options.transform = JXFORM_ROT_90;
  } else if (angle == 180) {
    options.transform = JXFORM_ROT_180;
  } else if ((angle ==-90) || (angle==270)) {
    options.transform = JXFORM_ROT_270;
  }

  jint rv = wrapper(env, bufferIn, size, outBuf, &options);
  (*env)->ReleaseByteArrayElements(env, jbyteArrayIn, (jbyte*) bufferIn, JNI_ABORT);
  return rv;
}






/*
 * wrapper: the method that executes the actual transformation
 *
 */
#define BUF_SIZE        1000000

jint wrapper(JNIEnv *env, unsigned char *in_data_p, int in_data_len, jobject outBuf, jpeg_transform_info *transformoption) {
  struct jpeg_decompress_struct srcinfo;
  struct jpeg_compress_struct dstinfo = {0};
  ;
  /*jpeg_transform_info transformoption;*/
  jvirt_barray_ptr *src_coefs = NULL;
  jvirt_barray_ptr *dst_coefs = NULL;
  unsigned long out_data_len = 0;
  unsigned char *out_data_p = NULL;

  struct jpeg_error_mgr srcerr;
  srcinfo.err = jpeg_std_error(&srcerr);
  struct jpeg_error_mgr dsterr;
  dstinfo.err = jpeg_std_error(&dsterr);

  /* Read input */
  jpeg_create_decompress(&srcinfo);
  jpeg_create_compress(&dstinfo);
  jpeg_mem_src(&srcinfo, in_data_p, in_data_len);
  jcopy_markers_setup(&srcinfo, JCOPYOPT_ALL);
  jpeg_read_header(&srcinfo, TRUE);

  /* Call the wrapped function with the transformoption struct */
  /* transformoption = func(self, *args, **kwargs) todo parameter */

  /* Prepare transformation */
  jtransform_request_workspace(&srcinfo, transformoption);
  src_coefs = jpeg_read_coefficients(&srcinfo);
  jpeg_copy_critical_parameters(&srcinfo, &dstinfo);
  dst_coefs = jtransform_adjust_parameters(&srcinfo, &dstinfo, src_coefs, transformoption);
  jpeg_mem_dest(&dstinfo, &out_data_p, &out_data_len);
  jpeg_write_coefficients(&dstinfo, dst_coefs);
  jcopy_markers_execute(&srcinfo, &dstinfo, JCOPYOPT_ALL);

  jtransform_execute_transform(&srcinfo, &dstinfo, src_coefs, transformoption);

  jpeg_finish_compress(&dstinfo);
  jpeg_destroy_compress(&dstinfo);
  jpeg_finish_decompress(&srcinfo);
  jpeg_destroy_decompress(&srcinfo);

  memcpy((unsigned char*) (*env)->GetDirectBufferAddress(env, outBuf), out_data_p, out_data_len);

  if(out_data_p != NULL)
    free(out_data_p);
  return (jint) out_data_len;
}

void init_transformoptions(jpeg_transform_info *transformoption) {
  transformoption->transform = JXFORM_NONE;
  transformoption->perfect = FALSE;
  transformoption->trim = FALSE;
  transformoption->force_grayscale = FALSE;
  transformoption->crop = FALSE;
  return;
}

