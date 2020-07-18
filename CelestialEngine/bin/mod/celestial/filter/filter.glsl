
#staticColorF

out_Color = vec4(uni_staticColor.rgb, 1.0);

#tintF

out_Color = mix(out_Color, vec4(uni_tintColor.rgb, out_Color.a), uni_tintBlendFactor);

#alphaF

out_Color = vec4(out_Color.rgb, mix(out_Color.a, uni_alpha, uni_alphaBlendFactor));

#normalOverrideV

in_normal = uni_normalOverride;

#projectiveMappingF

glb_texCoords = (att_clipSpacePosition.xy / att_clipSpacePosition.w) / 2.0 + 0.5;

#gradientV

float amount = dot(in_position, uni_gradientAxis);
float blendFactor = clamp((amount - uni_gradientBounds.x) / (uni_gradientBounds.y - uni_gradientBounds.x), 0.0, 1.0);
att_gradientColor = mix(uni_gradientColor0, uni_gradientColor1, blendFactor);
att_gradientBlend = mix(uni_gradientBlend0, uni_gradientBlend1, blendFactor);

#gradientF

out_Color = mix(out_Color, uni_gradientUseAlpha > 0.5 ? att_gradientColor : vec4(att_gradientColor.rgb, out_Color.a), att_gradientBlend);

#fogEffectV

float distance = length((uni_viewMatrix * WORLD_POSITION).xyz);
att_fogEffectVisibility = clamp(exp(-pow((distance * uni_fogEffectDensity), uni_fogEffectGradient)), 0.0, 1.0);

#fogEffectF

float visibility = clamp(1 - pow(gl_FragCoord.z, 200), 0.0, 1.0);
out_Color = mix(vec4(uni_fogEffectColor, out_Color.a), out_Color, att_fogEffectVisibility);

#fragFogEffectF

float visibility = clamp(1 - pow(gl_FragCoord.z, uni_fogEffectExponentiation), 0.0, 1.0);
out_Color = mix(vec4(uni_fogEffectColor, out_Color.a), out_Color, visibility);

#meshDistortionV

float amount = dot(in_position, uni_meshDistortionAxis);
float intensity = (amount - uni_meshDistortionBounds.x) / (uni_meshDistortionBounds.y - uni_meshDistortionBounds.x);
if(uni_meshDistortionSineWave.x > 0.5) glb_vertDistort += uni_meshDistortionVector * sin(intensity / (2 * 3.14) + uni_meshDistortionSineWave.y);
else glb_vertDistort += uni_meshDistortionVector * clamp(intensity, 0.0, 1.0);

#linearDepthV

att_linearDepthDistance = length(WORLD_POSITION.xyz - uni_linearDepthCenter) / uni_linearDepthFarPlane;

#linearDepthF

out_Color = vec4(vec3(clamp(att_linearDepthDistance, 0.0, 1.0)), out_Color.a);

#cubeMapDiffuseV

att_position = in_position;

#cubeMapDiffuseF

out_Color = texture(uni_cubeMapDiffuseSampler, normalize(att_position));
