/*Locationfunctionshere*/
function getUserLocation(){
	
	//checkifthegeolocationobjectissupported,ifsogetposition
	if(navigator.geolocation){
		navigator.geolocation.getCurrentPosition(displayLocation,displayError);
	}else{
		alert("Sorry, your browser doesn't support geo location!");
	}
}
function displayLocation(position){
	//buildtextstringincludingco-ordinatedatapassedinparameter
	$('#locationpreview').html("");
	var displayText="Userlatitudeis"+position.coords.latitude+"andlongitudeis"+position.coords.longitude;

	//baidu map api39.991801921039,116.47066251787
	var myGeo = new BMap.Geocoder();  
	// 根据坐标得到地址描述
	var point= new BMap.Point(position.coords.longitude,position.coords.latitude);
	BMap.Convertor.translate(point,2,function(point){
		myGeo.getLocation(point, function(result){  
 			if (result){
 	  			$("#locationpreview").append(result.address+"<a onclick=\"clearlocation()\">×</a>"); 
 			}  
		});		
	});
}
function clearlocation(){
	$('#locationpreview').html("");
}

function displayError(error) {

	//get a reference to the HTML element forwriting result

	var locationElement =document.getElementById("locationData");

	//find out which error we have, outputmessage accordingly

	switch(error.code) {

	case error.PERMISSION_DENIED:

	locationElement.innerHTML= "Permission was denied";

	break;

	case error.POSITION_UNAVAILABLE:

	locationElement.innerHTML= "Location data not available";

	break;

	case error.TIMEOUT:

	locationElement.innerHTML= "Location request timeout";

	break;

	case error.UNKNOWN_ERROR:

	locationElement.innerHTML= "An unspecified error occurred";

	break;

	default:

	locationElement.innerHTML= "Who knows what happened...";

	break;

	}
}
