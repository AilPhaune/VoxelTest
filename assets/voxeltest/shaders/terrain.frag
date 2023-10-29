#ifdef GL_ES
precision mediump float;
#endif

#define MAX_DYNAMIC_LIGHTS 256

uniform vec4 u_ambientColor;

uniform vec4 u_dynamicLightColor[MAX_DYNAMIC_LIGHTS];
uniform vec3 u_dynamicLightPos[MAX_DYNAMIC_LIGHTS];
uniform int u_dynamicLightCount;

varying vec2 v_texCoord;
varying vec4 v_light;
varying vec3 v_normal;
varying vec4 v_screenPosition;
varying vec4 v_scenePosition;
varying vec4 v_color;
varying vec3 v_toCameraPosition;

uniform sampler2D u_texture;

vec4 calculateDynamicLight() {
	vec4 value = vec4(0, 0, 0, 0);
	
	for(int i = 0; i < u_dynamicLightCount; i++) {
		float dist = length((u_dynamicLightPos[i] - v_scenePosition).xyz);
		float strength = dist <= 16.0 ? 0.5 : (1.0 - dist / 32.0);
		if(strength > 0) {
			vec4 light = strength * u_dynamicLightColor[i];
			value = vec4(max(light.r, value.r), max(light.g, value.g), max(light.b, value.b), max(light.a, value.a));
		}
	}
	
	return value;
}

/* old code

	float dist = length(v_toCameraPosition);
	float strength = dist <= 16.0 ? 0.5 : (1.0 - dist / 32.0);
	if(strength > 0) {
		vec4 playerGlow = strength * u_cameraEmitedColor;
		finalColor *= 1 - playerGlow;
		finalColor += playerGlow;
	}
	
*/

void main() {
	vec4 finalColor = u_ambientColor;
	
	finalColor += v_light * (1 - u_ambientColor);
    
	vec3 textureColor;
	
	if(v_texCoord.x == -1 && v_texCoord.y == -1) {
		textureColor = v_color;
	} else {
		textureColor = texture2D(u_texture, v_texCoord).rgb;
	}
	
	vec4 dynamicLight = calculateDynamicLight();
	finalColor *= 1 - dynamicLight;
	finalColor += dynamicLight;
	
    gl_FragColor = finalColor * vec4(textureColor, 1.0);
}