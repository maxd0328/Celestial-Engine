
#reinhardToneMapV
\#version 140

in vec2 in_position;

out vec2 textureCoords;

void main(void)
{
	gl_Position = vec4(in_position, 0.0, 1.0);
	textureCoords = in_position * 0.5 + 0.5;
}

#reinhardToneMapF
\#version 140

in vec2 textureCoords;

out vec4 out_Color;

uniform sampler2D uni_color;
uniform float uni_exposure;

void main(void)
{
	vec4 inColorRGBA = texture(uni_color, textureCoords);
	vec3 inColor = inColorRGBA.rgb;
	
	// Tone mapping
	vec3 toneMapped = vec3(1.0) - exp(-inColor * uni_exposure);
	
	out_Color = vec4(toneMapped, inColorRGBA.a);
}
