
#compositeV
\#version 140

in vec2 in_position;

out vec2 textureCoords;

void main(void)
{
	gl_Position = vec4(in_position, 0.0, 1.0);
	textureCoords = in_position * 0.5 + 0.5;
}

#additiveCompositeF
\#version 140

in vec2 textureCoords;

out vec4 out_Color;

uniform sampler2D uni_bottom;
uniform sampler2D uni_top;

void main(void)
{
	vec4 bottom = texture(uni_bottom, textureCoords);
	vec4 top = texture(uni_top, textureCoords);
	
	out_Color = vec4(bottom.rgb + (top.rgb * top.a), bottom.a);
}

#multiplicativeCompositeF
\#version 140

in vec2 textureCoords;

out vec4 out_Color;

uniform sampler2D uni_bottom;
uniform sampler2D uni_top;

void main(void)
{
	vec4 bottom = texture(uni_bottom, textureCoords);
	vec4 top = texture(uni_top, textureCoords);
	
	out_Color = vec4(bottom.rgb * mix(top.rgb, vec3(1), clamp(1 - top.a, 0.0, 1.0)), bottom.a);
}

#blendCompositeF
\#version 140

in vec2 textureCoords;

out vec4 out_Color;

uniform sampler2D uni_bottom;
uniform sampler2D uni_top;
uniform sampler2D uni_blendFactor;

void main(void)
{
	vec4 bottom = texture(uni_bottom, textureCoords);
	vec4 top = texture(uni_top, textureCoords);
	vec4 blendFactorRGBA = texture(uni_blendFactor, textureCoords);
	
	float blendFactor = clamp((blendFactorRGBA.r + blendFactorRGBA.g + blendFactorRGBA.b) / 3.0, 0.0, 1.0);
	
	out_Color = mix(bottom, top, blendFactor);
}

