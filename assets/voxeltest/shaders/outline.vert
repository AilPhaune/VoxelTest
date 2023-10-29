attribute vec4 a_position; // Vertex position

uniform mat4 u_projTrans; // Projection and transformation matrix
uniform vec3 u_offset;

void main() {
	vec4 realPos = a_position + vec4(u_offset, 0);
	gl_Position = vec4(u_projTrans * realPos);
}