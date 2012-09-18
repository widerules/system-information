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

function inject(data) {
    if (data.indexOf('::::') > 0) {
	var tmp = data.split('::::');
	var array = tmp[1].split('....');
	var title = document.getElementById('title' + tmp[0]);
	var content = document.getElementById('content' + tmp[0]);
	var arrayLength = array.length - 1;// the last element is noise
	
	for (i = 0; i < arrayLength; i++) {
		if (content.children.length <= i) {
			var li=document.createElement('li');
			li.outerHTML = array[i];
			content.appendChild(li);
		} 
		else if (content.children[i].outerHTML != array[i]) content.children[i].outerHTML = array[i]; 
	}

	while (content.children.length > arrayLength) {
		child = content.children[arrayLength];
		content.removeChild(child);
	}

	if (arrayLength == 0) title.style.display = 'none';
	else title.style.display = '';
    }
}

