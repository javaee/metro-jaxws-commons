/**
 * reflection.js v1.6
 *
 *
 * Contributors: Cow http://cow.neondragon.net
 *               Gfx http://www.jroller.com/page/gfx/
 *               Sitharus http://www.sitharus.com
 *               Andreas Linde http://www.andreaslinde.de
 *               Tralala, coder @ http://www.vbulletin.org
 *               Customized by:
 *               Greg Murray @ http://ajax.dev.java.net
 *
 * Freely distributable under MIT-style license.
 */
 
var Reflection = {
	defaultHeight : 0.5,
	defaultOpacity: 0.5,
	
	add: function(image, options) {
		Reflection.remove(image);
		
		doptions = { "height" : Reflection.defaultHeight, "opacity" : Reflection.defaultOpacity }
		if (options) {
			for (var i in doptions) {
				if (!options[i]) {
					options[i] = doptions[i];
				}
			}
		} else {
			options = doptions;
		}
	
		try {
			var d = document.createElement('div');
			var p = image;
        
			var classes = p.className.split(' ');
			var newClasses = '';
			for (j=0;j<classes.length;j++) {
					if (newClasses) {
						newClasses += ' '
					}
					newClasses += classes[j];
			}

			var reflectionHeight = Math.floor(p.height*options['height']);
			var divHeight = Math.floor(p.height*(1+options['height']));
			
			var reflectionWidth = p.width;
			
			if (document.all && !window.opera) {
				/* Copy original image's classes & styles to div */
				d.className = newClasses;
				
				d.style.cssText = p.style.cssText;
				p.style.cssText = 'vertical-align: bottom';
			
				var reflection = document.createElement('img');
				reflection.src = p.src;
				reflection.style.width = reflectionWidth+'px';
				
				reflection.style.marginBottom = "-"+(p.height-reflectionHeight)+'px';
				reflection.style.filter = 'flipv progid:DXImageTransform.Microsoft.Alpha(opacity='+(options['opacity']*100)+', style=1, finishOpacity=0, startx=0, starty=0, finishx=0, finishy='+(options['height']*100)+')';
				
				d.style.width = reflectionWidth+'px';
				d.style.height = divHeight+'px';
				p.parentNode.replaceChild(d, p);
				d.appendChild(p);
				d.appendChild(reflection);
			} else {
				var canvas = document.createElement('canvas');
				if (canvas.getContext) {
					/* Copy original image's classes & styles to div */
					d.className = newClasses;
					
					d.style.cssText = p.style.cssText;
					p.style.cssText = 'vertical-align: bottom';
			
					var context = canvas.getContext("2d");
				
					canvas.style.height = reflectionHeight+'px';
					canvas.style.width = reflectionWidth+'px';
					canvas.height = reflectionHeight;
					canvas.width = reflectionWidth;
					
					d.style.width = reflectionWidth+'px';
					d.style.height = divHeight+'px';
					p.parentNode.replaceChild(d, p);
					
					d.appendChild(p);
					d.appendChild(canvas);
					
					context.save();
					
					context.translate(0,image.height-1);
					context.scale(1,-1);
					
					context.drawImage(image, 0, 0, reflectionWidth, image.height);
	
					context.restore();
					
					context.globalCompositeOperation = "destination-out";
					var gradient = context.createLinearGradient(0, 0, 0, reflectionHeight);
					
					gradient.addColorStop(1, "rgba(255, 255, 255, 1.0)");
					gradient.addColorStop(0, "rgba(255, 255, 255, "+(1-options['opacity'])+")");
		
					context.fillStyle = gradient;
					if (navigator.appVersion.indexOf('WebKit') != -1) {
						context.fill();
					} else {
						context.fillRect(0, 0, reflectionWidth, reflectionHeight*2);
					}
				}
			}
		} catch (e) {
	    }
	},

	remove : function(image) {
			image.className = image.parentNode.className;
			image.parentNode.parentNode.replaceChild(image, image.parentNode);
	}
}
