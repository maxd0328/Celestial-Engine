
#gaussianHBlurV
\#version 140

in vec2 in_position;

out vec2 centerTexCoords;
out vec2 interval;

uniform float uni_targetWidth;

void main(void)
{
	gl_Position = vec4(in_position, 0.0, 1.0);
	
	centerTexCoords = in_position * 0.5 + 0.5;
	interval = vec2(1.0 / uni_targetWidth, 0.0);
}

#gaussianVBlurV
\#version 140

in vec2 in_position;

out vec2 centerTexCoords;
out vec2 interval;

uniform float uni_targetHeight;

void main(void)
{
	gl_Position = vec4(in_position, 0.0, 1.0);
	
	centerTexCoords = in_position * 0.5 + 0.5;
	interval = vec2(0.0, 1.0 / uni_targetHeight);
}

#gaussianBlurF
\#version 140

in vec2 centerTexCoords;
in vec2 interval;

out vec4 out_Color;

uniform sampler2D uni_color;
uniform float uni_kernelRadius;
uniform float uni_sigma;

float gaussianDistribution(float x, float sigma, float mean)
{
	float PI = 3.1415926535;
	float n = 1.0 / (sigma * sqrt(2 * PI));
	float d = x - mean;
	return n * exp(-0.5 * pow(d / sigma, 2));
}

void main(void)
{
	int kernelRadius = int(max(uni_kernelRadius, 1.0));
	out_Color = vec4(0.0);
	
	for(int i = -kernelRadius + 1 ; i < kernelRadius ; ++i)
	{
		int index = i + kernelRadius - 1;
		float gaussian = gaussianDistribution(i, uni_sigma, 0);
		out_Color += texture(uni_color, centerTexCoords + i * interval) * gaussian;
	}
}

#luminanceExtractionV
\#version 140

in vec2 in_position;

out vec2 textureCoords;

void main(void)
{
	gl_Position = vec4(in_position, 0.0, 1.0);
	textureCoords = in_position * 0.5 + 0.5;
}

#luminanceExtractionF
\#version 140

in vec2 textureCoords;

out vec4 out_Color;

uniform sampler2D uni_color;
uniform sampler2D uni_threshold;

void main(void)
{
	vec4 thresholdTex = texture(uni_threshold, textureCoords);
	float threshold = (thresholdTex.r + thresholdTex.g + thresholdTex.b) / 3.0;
	
	vec4 color = texture(uni_color, textureCoords);
	float luminance = (color.r + color.g + color.b) / 3.0;
	
	if(luminance >= threshold)
	{
		out_Color = color;
	}
	else
	{
		out_Color = vec4(0, 0, 0, color.a);
	}
}

#brightnessContrastV
\#version 140

in vec2 in_position;

out vec2 textureCoords;

void main(void)
{
	gl_Position = vec4(in_position, 0.0, 1.0);
	textureCoords = in_position * 0.5 + 0.5;
}

#brightnessContrastF
\#version 140

in vec2 textureCoords;

out vec4 out_Color;

uniform sampler2D uni_color;
uniform float uni_brightness;
uniform float uni_contrast;

void main(void)
{
	out_Color = texture(uni_color, textureCoords);
	out_Color.rgb *= uni_brightness;
	out_Color.rgb = (out_Color.rgb - 0.5) * uni_contrast + 0.5;
}
