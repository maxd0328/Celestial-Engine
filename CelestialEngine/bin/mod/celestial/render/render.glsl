
#planarV

att_texCoords = in_texCoords;
att_planarCameraVec = (inverse(uni_viewMatrix) * vec4(0, 0, 0, 1)).xyz - WORLD_POSITION.xyz;

#planarReflectionF

vec2 new_texCoords = (att_clipSpacePosition.xy / att_clipSpacePosition.w) / 2.0 + 0.5;
vec2 distortCoords = DISTORT_COORDS(new_texCoords, uni_planarReflectionDistortUnit);
float fresnelEffect = pow(1.0 - clamp(dot(normalize(att_planarCameraVec), uni_planarReflectionNormal), 0, 1), uni_planarReflectionBlend.z);
float blendFactor = (uni_planarReflectionBlend.y > 0.5 ? fresnelEffect : 1.0) * uni_planarReflectionBlend.x;
out_Color = mix(out_Color, vec4(texture(uni_planarReflection, distortCoords).rgb, out_Color.a), blendFactor);

#planarRefractionF

vec2 new_texCoords = (att_clipSpacePosition.xy / att_clipSpacePosition.w) / 2.0 + 0.5;
vec2 distortCoords = DISTORT_COORDS(new_texCoords, uni_planarRefractionDistortUnit);
float fresnelEffect = pow(clamp(dot(normalize(att_planarCameraVec), uni_planarRefractionNormal), 0, 1), uni_planarRefractionBlend.z);
float blendFactor = (uni_planarRefractionBlend.y > 0.5 ? fresnelEffect : 1.0) * uni_planarRefractionBlend.x;
out_Color = mix(out_Color, vec4(texture(uni_planarRefraction, distortCoords).rgb, out_Color.a), blendFactor);

#cubicEnvironmentV

vec3 surfaceNormal = normalize((uni_transform * vec4(in_normal, 0)).xyz);
vec3 invCameraVec = normalize(WORLD_POSITION.xyz - (inverse(uni_viewMatrix) * vec4(0, 0, 0, 1)).xyz);
att_cubicEnvironmentReflect = reflect(invCameraVec, surfaceNormal);
att_cubicEnvironmentRefract = refract(invCameraVec, surfaceNormal, uni_cubicEnvironmentRefractRatio);
att_cubicEnvironmentRefractBlend = uni_cubicEnvironmentRefractBlend.x;
if(uni_cubicEnvironmentRefractBlend.y > 0.5) att_cubicEnvironmentRefractBlend *= pow(clamp(dot(-invCameraVec, surfaceNormal), 0, 1), uni_cubicEnvironmentRefractBlend.z);

#cubicEnvironmentF

vec2 distortCoords = DISTORT_COORDS(vec2(0), uni_cubicEnvironmentDistortUnit);
vec3 vecAdd = vec3(distortCoords.x, distortCoords.y, distortCoords.x);

vec4 reflectColor = texture(uni_cubicEnvironment, normalize(att_cubicEnvironmentReflect + vecAdd));
vec4 refractColor = texture(uni_cubicEnvironment, normalize(att_cubicEnvironmentRefract + vecAdd));
out_Color = mix(out_Color, vec4(mix(reflectColor, refractColor, att_cubicEnvironmentRefractBlend).rgb, out_Color.a), uni_cubicEnvironmentBlend);

#depthEffectV

att_texCoords = in_texCoords;

#depthEffectF

vec2 new_texCoords = (att_clipSpacePosition.xy / att_clipSpacePosition.w) / 2.0 + 0.5;
vec2 distortCoords = DISTORT_COORDS(new_texCoords, uni_depthEffectDistortUnit);
float curDepth = texture(uni_depthEffect, distortCoords).r;
float absDistance = 2.0 * uni_depthEffectPlane.x * uni_depthEffectPlane.y / (uni_depthEffectPlane.y + uni_depthEffectPlane.x - (2.0 * curDepth - 1.0) * (uni_depthEffectPlane.y - uni_depthEffectPlane.x));

curDepth = gl_FragCoord.z;
float surfaceDistance = 2.0 * uni_depthEffectPlane.x * uni_depthEffectPlane.y / (uni_depthEffectPlane.y + uni_depthEffectPlane.x - (2.0 * curDepth - 1.0) * (uni_depthEffectPlane.y - uni_depthEffectPlane.x));
float depth = absDistance - surfaceDistance;

if(uni_depthEffectBlend.x > 0.5)
	out_Color = mix(out_Color, vec4(uni_depthEffectBlendColor, out_Color.a), uni_depthEffectBlend.z > 0.5 ? 1 - clamp(depth / uni_depthEffectBlend.y, 0, 1) : clamp(depth / uni_depthEffectBlend.y, 0, 1));
if(uni_depthEffectAlpha.x > 0.5) out_Color.a = uni_depthEffectAlpha.z > 0.5 ? 1 - clamp(depth / uni_depthEffectAlpha.y, 0, 1) : clamp(depth / uni_depthEffectAlpha.y, 0, 1);
if(uni_depthEffectDiffuse.x > 0) out_Color = mix(out_Color, vec4(vec3(depth / uni_depthEffectDiffuse.y), out_Color.a), uni_depthEffectDiffuse.x);
