
#outputV
\#version 140

in vec2 in_position;

out vec2 textureCoords;

void main(void)
{
	gl_Position = vec4(in_position, 0.0, 1.0);
	textureCoords = in_position * 0.5 + 0.5;
}

#outputF
\#version 140

in vec2 textureCoords;

out vec4 out_Color;

uniform sampler2D uni_color;

void main(void)
{
	out_Color = texture(uni_color, textureCoords);
//	out_Color.r = pow(out_Color.r, 35.0); // For depth buffer
//	out_Color.g = out_Color.r;
//	out_Color.b = out_Color.r;
}
