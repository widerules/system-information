function collapse(_1){ 
	title=document.getElementById("title"+_1).firstChild; 
	obj=document.getElementById("content"+_1); 
	collapsed=(obj.style.display==="none"); 
	window.JSinterface.saveCollapseState(_1,!collapsed); 
	if(collapsed){ 
		obj.style.display=""; 
		title.nodeValue="-"+title.nodeValue.substring(1); 
	}else{ 
		obj.style.display="none"; 
		title.nodeValue="+"+title.nodeValue.substring(1); 
	} 
}

function test(data) {
alert(data);
	if (data.indexOf('::::') > 0) {
		var tmp = data.split('::::');
		var name = "content" + tmp[0];
		var value = tmp[1];
alert(value);	
		var array = value.split('....');
		var title = document.getElementById(name);

		for (i = 0; i < array.length-1; i++) {
			var item = array[i];
			if (title.children.length <= i) {
				var li=document.createElement("li");
				li.innerHTML = item;
				title.appendChild(li);
			}
			else
				title.children[i].innerHTML = item;
		}

		var arrayLength = array.length;
		whild (title.children.length > arrayLength) {// remove extra child
			child = title.children[arrayLength];
			title.removeChild(child);
		}
	}
}
