[#ftl]
[#include "/template/head.ftl"/]
[@sj.head /]

<BODY LEFTMARGIN="0" TOPMARGIN="0">
<table width="100%" border="0">
    <tr>
      <td class="infoTitle" width="20%" style="height:22px;">
       <img src="${base}/static/images/action/info.gif" align="top"/><B>
          <B>照片库信息</B>
      </td>
      <td class="infoTitle" width="20%" style="height:22px;">
 		<font color="red">&nbsp;[@s.actionmessage/]</font>
      </td>
    </tr>
</table>
<div align="center">
	<div class="result ui-widget-content ui-corner-all" style="width:700px" align="left">
	   <table width="100%">
	     <tr>
	       <td align="right" width="20%">照片库信息:</td><td>${avatarBase.description!}</td>
	     </tr>
	     <tr>
	       <td align="right">照片总数量:</td><td>${names.total!}</td>
	     </tr>
	     [#if !avatarBase.readOnly]
	     <tr>
	       <td align="right">打包上传</td><td>
		    [@s.form name="uploadForm" action="board!uploadBatch.action" theme="simple" method="POST"  enctype="multipart/form-data"]
		        [@s.file name="avatar"/][@s.submit value="提交" /]
		    [/@s.form]
   	       </td>
	     </tr>
	     <tr>
	       <td colspan="2" align="center">上传文件为zip，不接受rar。大小在50M以内</td>
	     </tr>
	     [/#if]
	   <table>
	</div>

	<div class="result ui-widget-content ui-corner-all" style="width:700px" align="left">
	<table align="center" id="avatarPanel" width="90%">
	    [#list names?chunk(5) as nameList]
	    <tr>
	      [#list nameList as name]
	      <td style="padding:5px" align="center">
	          <img src="${base}/avatar/user.action?user.name=${name}" title="${name}" alt="${name}" width="120px" align="top"><br>
	          <a href="board!info.action?user.name=${name}" title="点击查看照片详情">${name}</a>
	      </td>
	      [/#list]
	   </tr>
	   [/#list]
	   [@b.pagebar pageId="avatarPanel" curPage=names/]
	</table>
	</div>
</div>
<form name="queryForm" action="${base}/avatar/board.action" method="post"/>
<script>
function pageGoWithSize(pageNo,pageSize){
  goToPage(queryForm,pageNo,pageSize);
}
</script>
</body>
[#include "/template/foot.ftl"/]
