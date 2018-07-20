package cc.lotuscard.api;


import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cc.lotuscard.bean.HttpResponse;
import cc.lotuscard.bean.LoginTokenData;
import cc.lotuscard.bean.MultipartBeanWithUserData;
import cc.lotuscard.bean.PartsData;
import cc.lotuscard.bean.QualityData;
import cc.lotuscard.bean.RetQuality;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * des:ApiService
 * Created by xsf
 * on 2016.06.15:47
 */
//        https://www.jianshu.com/p/7687365aa946
//@Path： URL中有参数,如：
//        http://102.10.10.132/api/Accounts/{accountId}
//@Query：参数在URL问号之后,如：
//        http://102.10.10.132/api/Comments ? access_token={access_token}
//@QueryMap：相当于多个@Query
//@Field：用于POST请求，提交单个数据（不显示在网址中）
//@Body： 相当于多个@Field，以对象的形式提交
// Tip1
//        使用@Field时记得添加@FormUrlEncoded
// Tip2
//        若需要重新定义接口地址，可以使用@Url，将地址以参数的形式传入即可
public interface ApiService {

//    @GET("login")
//    Observable<BaseRespose<User>> login(@Query("username") String username, @Query("password") String password);
//
//    //新闻详情
//    @GET("nc/article/{postId}/full.html")
//    Observable<Map<String, NewsDetail>> getNewDetail(
//            @Header("Cache-Control") String cacheControl,//添加响应头（缓存的方式）
//            @Path("postId") String postId);
//
//    //新闻列表
//    //http://c.m.163.com/nc/article/headline/T1348647909107/0-20.html
//    @GET("nc/article/{type}/{id}/{startPage}-20.html")
//    Observable<Map<String, List<NewsSummary>>> getNewsList(
//            @Header("Cache-Control") String cacheControl,
//            @Path("type") String type, @Path("id") String id,
//            @Path("startPage") int startPage);
//
//    @GET
//    Observable<ResponseBody> getNewsBodyHtmlPhoto(
//            @Header("Cache-Control") String cacheControl,
//            @Url String photoPath);
//    //@Url，它允许我们直接传入一个请求的URL。这样以来我们可以将上一个请求的获得的url直接传入进来，baseUrl将被无视
//    // baseUrl 需要符合标准，为空、""、或不合法将会报错
//
//
//    @GET("data/福利/{size}/{page}")
//    Observable<GirlData> getPhotoList(
//            @Header("Cache-Control") String cacheControl,
//            @Path("size") int size,
//            @Path("page") int page);
//
//    //视频
//    @GET("nc/video/list/{type}/n/{startPage}-10.html")
//    Observable<Map<String, List<VideoData>>> getVideoList(
//            @Header("Cache-Control") String cacheControl,
//            @Path("type") String type,
//            @Path("startPage") int startPage);

    /**
     * Test Api
     */

//    @GET("clo/quality")
//    Observable<QualityData> getQuality(
//            @Query("id") String id
//    );

    //上传测试
    @FormUrlEncoded
    @POST("clo/compare")
    Observable<RetQuality> getUpLoadAfterChecked(
            @Field("list") Object[][] qualityDataList
    );

    /**
     * Release Api
     */

    //登录
    @FormUrlEncoded
    @POST("api/client/login")
    Observable<HttpResponse<LoginTokenData>> getTokenWithSignIn(
            @Field("mobile") String username,
            @Field("password") String password
    );

    //质检项目
    @GET("api/client/get_clothes")
    Observable<HttpResponse<PartsData>> getQuality(
            @Query("c_num") String num
    );

    //质检样衣
    @GET("api/third/samples/parts")
    Observable<HttpResponse<ArrayList<QualityData.Parts>>> getQualitySample(
            @Query("content") String id
    );

    //质检数据上传  (@Part上传字符串类型的参数时会多一对引号 : https://blog.csdn.net/qq_33215972/article/details/68950838)
    @Multipart
    @POST("api/client/qualities")
    Observable<HttpResponse> uploadQualityData(
            @PartMap Map<String, RequestBody> map,
            @Part MultipartBody.Part[] images
    );

}
