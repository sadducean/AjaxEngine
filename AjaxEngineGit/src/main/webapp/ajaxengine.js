if(window.$==null||window.jQuery==null){
	console.log('jquery 필수');
	alert('jquery 필수');
}else{
	var root=document.currentScript.getAttribute('root');
	if(!root||root=='')root='/';
	window.ae=window.AjaxEngine={
		initialized:false,
		root:root,
		mappings:["update", "insert", "search", "searchpage", "count", "existdb", "createdb", "existstbl", "rawsql", "createtbl", "session", "droptbl", "addcol", "del","deletefile","encrypt","decrypt","now","ip"],
		req:function(url,pm,succ,fail){
			url=this.root+'ajax/'+url;
			if(typeof pm =='function'){
				succ=pm;
				pm={};
			}
			if(!succ)succ=function(){};
			if(!fail)fail=function(){};
			$.ajax(url, {
			    data : JSON.stringify(pm),
			    contentType : 'application/json',
			    type : 'POST'
			}).done(succ).fail(fail);
		},
		syncreq:function(url,pm,fail){
			url=this.root+'ajax/'+url;
			if(typeof pm =='function'){
				succ=pm;
				pm={};
			}
			if(!fail)fail=function(){};
			var rslt;
			$.ajax(url, {
			    data : JSON.stringify(pm),
			    contentType : 'application/json',
			    type : 'POST',
				async:false
			}).done(function(res){
				rslt=res;
			}).fail(fail);
			return rslt;
		},
		test:function(pm,succ,fail){
			this.req('test',pm,succ,fail);
		}
	};
	for(var i=0;i<AjaxEngine.mappings.length;i++){
		var mp=AjaxEngine.mappings[i];
		AjaxEngine[mp]=new Function('pm','succ','fail','AjaxEngine.req("'+mp+'",pm,succ,fail);');
		AjaxEngine['sync'+mp]=new Function('pm','fail','return AjaxEngine.syncreq("'+mp+'",pm,fail);');
	}
	AjaxEngine.test=function(obj){
		$.ajax({
			url: ae.root+"ajax/test",
			type: "post",
			async:false,
			accept: "application/json",
			contentType: "application/json; charset=utf-8",
			data: JSON.stringify({'a': 1}),
			dataType: "json",
			success: function(data) {
				console.log(data);
			},
			error: function(jqXHR,textStatus,errorThrown) {
				console.log(jqXHR,textStatus,errorThrown);	
			}
			});
	};
	AjaxEngine.filechange=function(obj){
		this.uform.submit();
	};
	AjaxEngine.fileuploaded=function(res){
		if(res&&res.trim()!=''&&res.indexOf('{')>-1&&res.indexOf('}')>-1){
			res='{'+res.trim().split('{')[1].split('}')[0]+'}';
			this.uploadfilecallback(JSON.parse(res));
		}
	}
	
	AjaxEngine.uploadfile=function(callback){
		this.uploadfilecallback=callback;
		if(this.uform)this.uform.remove();
		this.uform=$('<form method="post" action="'+this.root+'ajax/file" target="AjaxEngineIfr" enctype="multipart/form-data" style="height:1px;overflow:hidden;">'
		+'<input name="act" value="upload"/>'
		+'<input name="file" type="file" onchange="AjaxEngine.filechange(this)"/>'
		+'</form>').appendTo('body');
		this.uform.find('[type="file"]')[0].click();
	};
	AjaxEngine.addifr=function(){
		if($('body').length==0){
			setTimeout(this.addifr,100);
		}else{
			$('body').prepend(
				'<iframe id="AjaxEngineIfr" name="AjaxEngineIfr" onload="AjaxEngine.fileuploaded(this.contentWindow.document.body.innerHTML)" src="'+AjaxEngine.root+'upload/uploadfolder.html" style="height:1px;overflow:hidden;border-width:0;opacity:0.001;position:absolute;"></iframe/>');	
		}
	};
	setTimeout(AjaxEngine.addifr,100);
	AjaxEngine.bulk=function(method,pms,ajaxcallback,endcallback){
		AjaxEngine.bulkcnt=0;
		AjaxEngine.bulkpms=pms;
		if(!method)method='get';
		method=method.toLowerCase();
		for(var i=0;i<pms.length;i++){
			var pm=pms[i];
			if(!pm.json)pm.json={};
			$.ajax({
				idx:i,
				method:method,
				url:pm.url,
				data:pm.json,
				success:function(responseData,textStatus,jqXHR){
					AjaxEngine.bulkcnt++;
					AjaxEngine.bulkpms[this.idx].responseData=responseData;
					if(ajaxcallback)ajaxcallback(this.idx,responseData);
					if(AjaxEngine.bulkcnt==AjaxEngine.bulkpms.length){
						endcallback(AjaxEngine.bulkpms);
					}
				}
			});
		}
	};
	AjaxEngine.syncbulk=function(method,pms,ajaxcallback,endcallback){
		AjaxEngine.bulkcnt=0;
		AjaxEngine.bulkpms=pms;
		if(!method)method='get';
		method=method.toLowerCase();
		for(var i=0;i<pms.length;i++){
			var pm=pms[i];
			if(!pm.json)pm.json={};
			$.ajax({
				idx:i,
				method:method,
				url:pm.url,
				data:pm.json,
				async:false,
				success:function(responseData,textStatus,jqXHR){
					AjaxEngine.bulkcnt++;
					AjaxEngine.bulkpms[this.idx].responseData=responseData;
					if(ajaxcallback)ajaxcallback(this.idx,responseData);
					if(AjaxEngine.bulkcnt==AjaxEngine.bulkpms.length){
						endcallback(AjaxEngine.bulkpms);
					}
				}
			});
		}
	};
	AjaxEngine.createtblproc=function(){
		if(!AjaxEngine.tblcandidates)return;
		if(AjaxEngine.tblcandidates.length==0){
			AjaxEngine.tblcandidates=null;
			AjaxEngine.tblcreateresultcallback(AjaxEngine.tblcreateresults);
			return;
		}
		var tblinfo=AjaxEngine.tblcandidates.shift();
		AjaxEngine.createtbl(tblinfo,function(data){
			AjaxEngine.tblcreateresults.push(data.success);
			AjaxEngine.createtblproc();
		});
	};
	AjaxEngine.createtbls=function(tbls,callback){
		AjaxEngine.tblcandidates=tbls;
		AjaxEngine.tblcreateresults=[];
		AjaxEngine.tblcreateresultcallback=callback;
		AjaxEngine.createtblproc();	
	};
	AjaxEngine.saferun=function(varname,func){
		if(window[varname]==null||window[varname]===false){
			setTimeout(function(){
				AjaxEngine.saferun(varname,func);
			},10);
		}else{
			func(window[varname]);
		}
	};
	ae.mapselector=function(lonselector,latselector,mapselector){
		ae.mslon=$(lonselector);
		ae.mslat=$(latselector);
		ae.msmap=new kakao.maps.Map($(mapselector)[0],{
			center:new kakao.maps.LatLng(ae.mslat.val(),ae.mslon.val())
		});
		ae.msmarker = new kakao.maps.Marker({ 
		    position: ae.msmap.getCenter() 
		}); 
		ae.msmarker.setMap(ae.msmap);
		kakao.maps.event.addListener(ae.msmap, 'click', function(mouseEvent) {        
		    var latlng = mouseEvent.latLng; 
		    ae.msmarker.setPosition(latlng);
		    ae.mslon.val(latlng.getLng());
			ae.mslat.val(latlng.getLat());
		});
		$(lonselector+','+latselector).change(function(){
			var latlng = new kakao.maps.LatLng(ae.mslat.val(),ae.mslon.val());
			ae.msmap.setCenter(latlng);
			ae.msmarker.setPosition(latlng);
		});
	};
	ae.renderpage=function(){
		if(ae.rendertbls.length==0){
			ae.renderendcall();
			return;
		}
		ae.rendertbl=ae.rendertbls.shift();
		ae.searchpage(ae.rendertbl,function(tbl){
			tbl=tbl.data;
			var rtbl=$(ae.rendertbl.tblselector).empty();
			rtbl.append('<thead/>');
			rtbl.append('<tbody/>');
			if(ae.rendertbl.tblattrs)rtbl.attr(ae.rendertbl.tblattrs);
			var htr=rtbl.find('thead').append('<tr/>');
			if(ae.rendertbl.hasindex){
				var nth=$('<th>번호</th>').appendTo(htr);
				if(ae.rendertbl.indexheadattrs)nth.attr(ae.rendertbl.indexheadattrs);
			}
			for(var j=0;j<ae.rendertbl.cols.length;j++){
				var col=ae.rendertbl.cols[j];
				var hth=$('<th>'+col.text+'</th>').appendTo(htr);
				if(col.headattrs)hth.attr(col.headattrs);
			}
			if(!ae.rendertbl.isindexasc)ae.rendertbl.isindexasc=true;
			var rows=tbl.values;
			for(var i=0;i<rows.length;i++){
				var row=rows[i];
				var tr=rtbl.find('tbody').append('<tr/>');
				if(ae.rendertbl.hasindex){
					var idx=tbl.pagination.rows*(tbl.pagination.page-1)+i+1;
					if(!ae.rendertbl.isindexasc){
						idx=tbl.pagination.ttlrow-idx+1;
					}
					var rntd=$('<td>'+idx+'</td>').appendTo(tr);
					if(ae.rendertbl.indexattrs)rntd.attr(ae.rendertbl.indexattrs);
				}
				for(var j=0;j<ae.rendertbl.cols.length;j++){
					var col=ae.rendertbl.cols[j];
					var rtd=$('<td>'+row[col.name]+'</td>').appendTo(tr);
					if(col.rowattrs)rtd.attr(col.rowattrs);
				}
			}
			var psel=ae.rendertbl.pagewrapperselector;
			if(!ae.rgopage)ae.rgopage={};
			ae.rgopage[psel]=function(){};
			if(ae.rendertbl.gopage)ae.rgopage[psel]=ae.rendertbl.gopage;
			var pwrap=$(psel).empty();
			if(ae.rgopage[$(this).data('psel')])ae.rgopage[psel]=ae.rgopage[$(this).data('psel')];
			$(ae.rendertbl.firstpagebtnhtml).appendTo(pwrap).data({'page':1,'psel':psel}).click(function(){
				ae.rgopage[$(this).data('psel')]($(this).data('page'));
			});
			if(tbl.pagination.hasprev){
				$(ae.rendertbl.prevbtnhtml).appendTo(pwrap).data({'page':tbl.pagination.stpage-1,'psel':psel}).click(function(){
					ae.rgopage[$(this).data('psel')]($(this).data('page'));
				});
			}
			for(var i=tbl.pagination.stpage;i<=tbl.pagination.edpage;i++){
				if(i==tbl.pagination.page){
					pwrap.append(ae.rendertbl.currentpagehtml.replace('_page_',i));
				}else{
					pwrap.append(ae.rendertbl.pagehtml.replace('_page_',i));
				}
				pwrap.find(':last-child').data({'page':i,'psel':psel}).click(function(){
					ae.rgopage[$(this).data('psel')]($(this).data('page'));
				});
			}
			if(tbl.pagination.hasnext){
				$(ae.rendertbl.nextbtnhtml).appendTo(pwrap).data({'page':tbl.pagination.edpage+1,'psel':psel}).click(function(){
					ae.rgopage[$(this).data('psel')]($(this).data('page'));
				});
			}
			pwrap.append(ae.rendertbl.lastpagebtnhtml);
			$(ae.rendertbl.lastpagebtnhtml).appendTo(pwrap).data({'page':tbl.pagination.ttlpage,'psel':psel}).click(function(){
				ae.rgopage[$(this).data('psel')]($(this).data('page'));
			});
			if(ae.rendertbl.callback)ae.rendertbl.callback(tbl,rtbl,pwrap);
			ae.renderpage();
		});
	};
	ae.renderpages=function(tbls,endcall){
		if(!Array.isArray(tbls))tbls=[tbls];
		ae.rendertbls=tbls;
		if(!endcall)endcall=function(){};
		ae.renderendcall=endcall;
		ae.renderpage();
	};
	ae.tableinfo=function(db,tablename){
		window.open(ae.root+'ajax/tableinfo?db='+db+'&tablename='+tablename);
	};
	ae.getip=function(){
		var rslt;
		$.ajax({url:ae.root+'ajax/ip',async:false,success:function(ip){rslt=ip;}});
		return rslt;
	};
	ae.post=function(url,pms,newwin){
		var fid='aef'+new Date().getTime(); 
		var form=$('<form/>').attr({'id':fid,'method':'post'}).css('display','none');
		if(newwin){
			form.attr('target','_blank');
		}
		for(var key in pms){
			form.append('<input type="hidden"/>').attr({key:pms[key]});
		}
		$('body').append(form);
		form.submit();
	};
	ae.join=function(pms){
		$.post(ae.root+'ajax/join'
			,{db:pms.db,cols:pms.cols+'',tbls:JSON.stringify(pms.tbls)
				,where:pms.where,orderby:pms.orderby}
			,pms.callback);
	}
	ae.syncjoin=function(pms){
		var rslt;
		$.ajax({
           type: 'POST',
           url: ae.root+'ajax/join',
           async:false,
           data: {db:pms.db,cols:pms.cols+'',tbls:JSON.stringify(pms.tbls)
			,where:pms.where,orderby:pms.orderby}
           ,success: function(data) {
                rslt=data;
           }
      });
      return rslt;
	}
	ae.sleep=(ms=>new Promise(resolve => setTimeout(resolve, ms)));
	ae.geocode= function(address,callback){
		ae.geocoderslt='';
		ae.geocodercallback=callback;
		new kakao.maps.services.Geocoder().addressSearch(address, function(result, status) {
		     if (status === kakao.maps.services.Status.OK) {
				ae.geocoderslt={lng:result[0].x, lat:result[0].y};
		    }else{
		    	ae.geocoderslt={status:status,result:result};
		    }
		    ae.geocodercallback(ae.geocoderslt);
		});
	};
	ae.reversegeocode= function(coord,callback){
		ae.rgeocoderslt='';
		ae.rgeocodercallback=callback;
		new kakao.maps.services.Geocoder().coord2RegionCode(coord.lng,coord.lat, function(result, status) {
		     if (status === kakao.maps.services.Status.OK) {
		     	for(var i = 0; i < result.length; i++) {
		            if (result[i].region_type === 'H') {
		                ae.rgeocoderslt = result[i];
		                break;
		            }
	        	}
		    }else{
		    	ae.rgeocoderslt={status:status,result:result};
		    }
		     ae.rgeocodercallback(ae.rgeocoderslt);
		});
	};
	ae.postcode=function(address,callback){
		address=encodeURIComponent(address);
		$.ajax({
		  "url": "https://dapi.kakao.com/v2/local/search/address.json?query="+address+"&page=1&size=1",
		  "method": "GET",
		  "timeout": 0,
		  "headers": {
		    "Authorization": "KakaoAK 9d84bf035c20e43d0ddf783e3dd6a75f"
		  },
		}).done(function (response) {
		  callback(response.documents[0]);
		});
	}
}
