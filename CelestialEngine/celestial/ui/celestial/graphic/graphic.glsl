#staticColorV

\#version 140

in vec2 in_position;

uniform mat4 uni_transform;

void main(void)
{
	gl_Position = uni_transform * vec4(in_position, 0.0, 1.0);
}

#staticColorF

\#version 140

out vec4 out_Color;

uniform vec3 uni_color;

void main(void)
{
	out_Color = vec4(uni_color, 1.0);
}
