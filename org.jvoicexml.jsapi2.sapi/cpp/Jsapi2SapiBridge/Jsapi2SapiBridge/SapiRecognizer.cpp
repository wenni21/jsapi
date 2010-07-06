#include "stdafx.h"
#include "org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer.h"
#include "Recognizer.h"
#include "JNIUtils.h"


/*
 * Class:     org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer
 * Method:    nativGetBuildInGrammars
 * Signature: (J)Ljava/util/Vector;
 */
JNIEXPORT jobject JNICALL Java_org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer_sapiGetBuildInGrammars
  (JNIEnv *env, jobject object, jlong recognizerHandle){

	return false;
}

/*
 * Class:     org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer
 * Method:    nativeHandleAllocate
 * Signature: (J)V
 */
JNIEXPORT jlong JNICALL Java_org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer_sapiAllocate
  (JNIEnv *env, jobject object)
{
    HRESULT hr = ::CoInitializeEx(NULL, COINIT_MULTITHREADED);
    if (FAILED(hr))
    {
        char buffer[1024];
        GetErrorMessage(buffer, sizeof(buffer), "Initializing COM failed!",
            hr);
        ThrowJavaException(env, "javax/speech/EngineException", buffer);
        return 0;
    }

    /* create new Recognizer class */
    Recognizer* recognizer = new Recognizer(hWnd, env, object);
    /* check if Handle valid */
    if (FAILED(recognizer->hr))
    {      
        char buffer[1024];
        GetErrorMessage(buffer, sizeof(buffer), "Allocation of recognizer failed",
            recognizer->hr);
        ThrowJavaException(env, "javax/speech/EngineException", buffer);
        return 0;
    }
	
    return (jlong) recognizer;	
}

/*
 * Class:     org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer
 * Method:    nativHandleDeallocate
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer_sapiDeallocate
  (JNIEnv *env, jobject object, jlong recognizerHandle){

	Recognizer* recognizer = (Recognizer*)recognizerHandle;
	delete recognizer;
}

/*
 * Class:     org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer
 * Method:    nativHandlePause
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer_sapiPause__J
  (JNIEnv *env, jobject object, jlong recognizerHandle){
		
	Recognizer* recognizer = (Recognizer*)recognizerHandle;
	recognizer->pause();
	
	if (FAILED(recognizer->hr))
    {
        char buffer[1024];
        GetErrorMessage(buffer, sizeof(buffer), "Pause recognizer failed",
            recognizer->hr);
        ThrowJavaException(env, "javax/speech/EngineException", buffer);
    }	
}

/*
 * Class:     org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer
 * Method:    nativHandlePause
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer_sapiPause__JI
  (JNIEnv *env, jobject object, jlong recognizerHandle, jint flags){

}

/*
 * Class:     org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer
 * Method:    sapiResume
 * Signature: (J[Ljava/lang/String;)Z
 */
JNIEXPORT jstring JNICALL Java_org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer_sapiResume
  (JNIEnv *env, jobject object, jlong handle, jobjectArray grammars)
{

	Recognizer* recognizer = (Recognizer*)handle;
    jsize size = env->GetArrayLength(grammars);
    for (jsize i=0; i<size; i++)
    {
        jstring grammar = (jstring) env->GetObjectArrayElement(grammars, i);
		const wchar_t* gram = (const wchar_t*)env->GetStringChars(grammar, NULL);	
		
		HRESULT hr= recognizer->LoadGrammarFile(gram);
        if ( FAILED(hr) )
        {
            char buffer[1024];
            GetErrorMessage(buffer, sizeof(buffer), "Resume recognizer failed",
                hr);
            ThrowJavaException(env, "javax/speech/EngineStateException", buffer);
        }
    }
	
	LPWSTR utterance =recognizer->Resume();

	int len = wcslen(utterance);
	jstring jstr = env->NewString((jchar*) utterance, wcslen(utterance));
		
	return jstr;
}

/*
 * Class:     org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer
 * Method:    nativSetGrammar
 * Signature: (JLjava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer_sapiSetGrammar
  (JNIEnv *env, jobject object, jlong recognizerHandle, jstring grammarPath){
		
	Recognizer* recognizer = (Recognizer*)recognizerHandle;
    const wchar_t* gram = (const wchar_t*)env->GetStringChars(grammarPath, NULL);
	HRESULT hr = recognizer->LoadGrammarFile(gram);
	if (SUCCEEDED(hr))
    {
        return JNI_TRUE;
	}
	else
    {
		return JNI_FALSE;
	}
}

/*
 * Class:     org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer
 * Method:    getChangeRequestListener
 * Signature: ()Lorg/jvoicexml/jsapi2/EnginePropertyChangeRequestListener;
 */
JNIEXPORT jobject JNICALL Java_org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer_getChangeRequestListener
 (JNIEnv *env, jobject object)
{
	return NULL;
}

/*
 * Class:     org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer
 * Method:    start
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_jvoicexml_jsapi2_sapi_recognition_SapiRecognizer_start
(JNIEnv *env, jobject object, jlong recognizerHandle)
{
	reinterpret_cast<Recognizer*>(recognizerHandle)->startdictation();	
}
