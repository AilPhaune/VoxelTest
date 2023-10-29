attribute vec4 a_position; // Vertex position
attribute vec3 a_normal;
attribute vec4 a_color;
attribute vec2 a_texCoord0; // UV coordinates
attribute float a_light;

uniform mat4 u_projTrans; // Projection and transformation matrix
uniform vec3 u_chunkOffset; // Position offset of the chunk
uniform vec3 u_cameraPosition;

varying vec2 v_texCoord; // UV coordinates to pass to the fragment shader
varying vec4 v_light;
varying vec3 v_normal;
varying vec4 v_screenPosition;
varying vec4 v_scenePosition;
varying vec4 v_color;
varying vec3 v_toCameraPosition;

vec4 getLight(float packedLightF) {
	int packedLight = int(packedLightF);
	int sky = 0xF & (packedLight >> 12);
	int r = 0xF & (packedLight >> 8);
	int g = 0xF & (packedLight >> 4);
	int b = 0xF & (packedLight >> 0);
	if(sky > r) r = sky;
	if(sky > g) g = sky;
	if(sky > b) b = sky;
	return vec4(r, g, b, 1) / 15.0f;
}

void main() {
	v_scenePosition = vec4(a_position.xyz + u_chunkOffset, a_position.w);
	v_toCameraPosition = (u_cameraPosition - v_scenePosition).xyz;
	v_screenPosition = u_projTrans * v_scenePosition;
	
    v_light = getLight(a_light);
    
    v_normal = a_normal;
    
    v_color = a_color;
    
    v_texCoord = a_texCoord0; // Pass the UV coordinates to the fragment shader
    
    gl_Position = v_screenPosition; // Transform the vertex position
}