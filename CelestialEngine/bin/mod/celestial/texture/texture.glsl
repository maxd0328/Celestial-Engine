
#mapV

att_texCoords = in_texCoords;

#mapF_glb

\#define ADDITIVE_MIRROR(sampler, coords, mirror) ( texture(sampler, coords) + (mirror.x > 0.5 ? texture(sampler, \
		   coords * vec2(-1, 1)) : vec4(0)) + (mirror.y > 0.5 ? texture(sampler, coords * vec2(1, -1)) : vec4(0)) )

// Scales distort ( * 2 - 1)
\#define SCALE_DISTORT(tex) ( (tex) * vec4(2.0, 2.0, 1.0, 1.0) - vec4(1.0, 1.0, 0.0, 0.0) )

// Additive mirror for distort (scales distortion ( * 2 - 1))
\#define ADDITIVE_MIRROR_D(sampler, coords, mirror) (  SCALE_DISTORT(texture(sampler, coords)) + (mirror.x > 0.5 ?  SCALE_DISTORT(texture(sampler, \
		   coords * vec2(-1, 1))) : vec4(0)) + (mirror.y > 0.5 ?  SCALE_DISTORT(texture(sampler, coords * vec2(1, -1))) : vec4(0)) )

\#define DISTORT_COORDS(coords, textureUnit) ( textureUnit == -1.0 ? coords : clamp(coords + (ADDITIVE_MIRROR_D(uni_distortionMap[int(textureUnit)], glb_texCoords * uni_distortionMapTile[int(textureUnit)] \
										 + uni_distortionMapOffset[int(textureUnit)], uni_distortionMapAdditiveMirror[int(textureUnit)]).rg * uni_distortionMapIntensity[int(textureUnit)]), 0.001, 0.999) )

// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
\#define ATLAS_MAP(enabled, atlas, map, tile, distort, offset, mirror) vec4 cur_Color;																														\
{																																																			\
	vec4 blendColor = ADDITIVE_MIRROR(uni_blendMap, DISTORT_COORDS(glb_texCoords, uni_blendMapDistortUnit) * uni_blendMapTile + uni_blendMapOffset, uni_blendMapMirror);									\
	vec4 rgbColor = vec4(0, 0, 0, 0);																																										\
																																																			\
	for(int i = 0 ; i < 4 ; ++i) {																																											\
		if(enabled[i] > 0.5) {																																												\
			vec2 distortCoords = DISTORT_COORDS(glb_texCoords, distort[i]);																																	\
			if(atlas[i].z == floor(atlas[i].z)) {																																							\
				vec2 off = vec2(int(atlas[i].z) % int(atlas[i].x) / float(atlas[i].x),																														\
								int(atlas[i].z) / int(atlas[i].x) / float(atlas[i].y));																														\
				if(i == 0) cur_Color = ADDITIVE_MIRROR(map[i], (vec2(distortCoords.x / atlas[i].x, distortCoords.y / atlas[i].y) + off) * tile[i] + offset[i], mirror[i]);									\
				else rgbColor += ADDITIVE_MIRROR(map[i], (vec2(distortCoords.x / atlas[i].x, distortCoords.y / atlas[i].y) + off) * tile[i] + offset[i], mirror[i])											\
																				* (i == 1 ? blendColor.r : i == 2 ? blendColor.g : blendColor.b);															\
			}																																																\
			else {																																															\
				int idx0 = int(atlas[i].z), idx1 = int(atlas[i].z + 1);																																		\
				vec2 off0 = vec2(idx0 % int(atlas[i].x) / float(atlas[i].x), idx0 / int(atlas[i].x) / float(atlas[i].y)),																					\
					 off1 = vec2(idx1 % int(atlas[i].x) / float(atlas[i].x), idx1 / int(atlas[i].x) / float(atlas[i].y));																					\
				vec4 col0 = ADDITIVE_MIRROR(map[i], (vec2(distortCoords.x / atlas[i].x, distortCoords.y / atlas[i].y) + off0) * tile[i] + offset[i], mirror[i]),											\
					 col1 = ADDITIVE_MIRROR(map[i], (vec2(distortCoords.x / atlas[i].x, distortCoords.y / atlas[i].y) + off1) * tile[i] + offset[i], mirror[i]);											\
				if(i == 0) cur_Color = mix(col0, col1, atlas[i].z - int(atlas[i].z));																														\
				else rgbColor += mix(col0, col1, atlas[i].z - int(atlas[i].z)) * (i == 1 ? blendColor.r : i == 2 ? blendColor.g : blendColor.b);															\
			}																																																\
		}																																																	\
	}																																																		\
																																																			\
	cur_Color = mix(cur_Color, rgbColor, (blendColor.r + blendColor.g + blendColor.b) * rgbColor.a);																										\
}
// ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

// END ====================================================================================================================================================================
#diffuseMapF

ATLAS_MAP(uni_diffuseMapEnabled, uni_diffuseMapAtlas, uni_diffuseMap, uni_diffuseMapTile, uni_diffuseMapDistortUnit, uni_diffuseMapOffset, uni_diffuseMapMirror);
out_Color = cur_Color;

// END ====================================================================================================================================================================
#illuminationMapF

ATLAS_MAP(uni_illuminationMapEnabled, uni_illuminationMapAtlas, uni_illuminationMap, uni_illuminationMapTile, uni_illuminationMapDistortUnit, uni_illuminationMapOffset, uni_illuminationMapMirror);
cur_Color *= uni_illuminationMapBlend;
if(uni_illuminationMapSingleChannel > 0.5) out_Color *= vec4(1.0, 1.0, 1.0, 1.0) + cur_Color * vec4(5.0, 5.0, 5.0, 1.0);
else out_Color += cur_Color;

// END ====================================================================================================================================================================
#bumpMapF

ATLAS_MAP(uni_bumpMapEnabled, uni_bumpMapAtlas, uni_bumpMap, uni_bumpMapTile, uni_bumpMapDistortUnit, uni_bumpMapOffset, uni_bumpMapMirror);
glb_surfaceNorm = mix(vec3(0, 0, 1), (2.0 * cur_Color - 1.0).rgb, uni_bumpMapIntensity);

// END ====================================================================================================================================================================
#metallicMapF

ATLAS_MAP(uni_metallicMapEnabled, uni_metallicMapAtlas, uni_metallicMap, uni_metallicMapTile, uni_metallicMapDistortUnit, uni_metallicMapOffset, uni_metallicMapMirror);
glb_materialMetallic = clamp((cur_Color.r + cur_Color.g + cur_Color.b) / 3.0 * uni_metallicMapIntensity, 0.0, 1.0);

// END ====================================================================================================================================================================
#roughnessMapF

ATLAS_MAP(uni_roughnessMapEnabled, uni_roughnessMapAtlas, uni_roughnessMap, uni_roughnessMapTile, uni_roughnessMapDistortUnit, uni_roughnessMapOffset, uni_roughnessMapMirror);
glb_materialRoughness = clamp((cur_Color.r + cur_Color.g + cur_Color.b) / 3.0 * uni_roughnessMapIntensity, 0.0, 1.0);

// END ====================================================================================================================================================================
#subsurfaceMapF

ATLAS_MAP(uni_subsurfaceMapEnabled, uni_subsurfaceMapAtlas, uni_subsurfaceMap, uni_subsurfaceMapTile, uni_subsurfaceMapDistortUnit, uni_subsurfaceMapOffset, uni_subsurfaceMapMirror);
glb_materialSubsurface = clamp((cur_Color.r + cur_Color.g + cur_Color.b) / 3.0 * uni_subsurfaceMapIntensity, 0.0, 1.0);

// END ====================================================================================================================================================================
#aoMapF

ATLAS_MAP(uni_aoMapEnabled, uni_aoMapAtlas, uni_aoMap, uni_aoMapTile, uni_aoMapDistortUnit, uni_aoMapOffset, uni_aoMapMirror);
glb_materialAO = clamp((cur_Color.r + cur_Color.g + cur_Color.b) / 3.0 * uni_aoMapIntensity, 0.0, 1.0);

// END ====================================================================================================================================================================
#pointDiffuseV

vec3 totalDiffuse = vec3(0);

vec3 worldPosition = WORLD_POSITION.xyz;
vec3 unormal = normalize((uni_transform * vec4(in_normal, 0)).xyz);

for(int i = 0 ; i < uni_materialCount ; ++i) {
	if(uni_materialPos[i].w == 2) {
		totalDiffuse += mix(uni_materialCol[i], uni_materialDiffuseTint, uni_materialDiffuseBlend);
		continue;
	}
	vec3 lightVec = uni_materialPos[i].w == 1 ? -uni_materialPos[i].xyz : uni_materialPos[i].xyz - worldPosition;
	float distance = length(lightVec);
	float attFactor = uni_materialPos[i].w != 0 ? 1 : uni_materialAtt[i].x + (uni_materialAtt[i].y * distance) + (uni_materialAtt[i].z * distance * distance);
	float ndotl = max(dot(unormal, normalize(lightVec)), 0);
	totalDiffuse += (ndotl * mix(uni_materialCol[i], uni_materialDiffuseTint, uni_materialDiffuseBlend)) / attFactor;
}

totalDiffuse *= 1 + uni_materialAmbient;
att_vertMaterialDiffuse = clamp(totalDiffuse * uni_materialDiffuse, uni_materialDiffuseMin, uni_materialDiffuseMax);

// END ====================================================================================================================================================================
#pointDiffuseF

out_Color *= vec4(att_vertMaterialDiffuse, 1);

// END ====================================================================================================================================================================
#vertMaterialV

vec3 totalDiffuse = vec3(0), totalSpecular = vec3(0);

vec3 worldPosition = WORLD_POSITION.xyz;
vec3 unormal = normalize((uni_transform * vec4(in_normal, 0)).xyz);
vec3 ucameraVec = normalize((inverse(uni_viewMatrix) * vec4(0, 0, 0, 1)).xyz - worldPosition);

for(int i = 0 ; i < uni_materialCount ; ++i) {
	if(uni_materialPos[i].w == 2) {
		totalDiffuse += mix(uni_materialCol[i], uni_materialDiffuseTint, uni_materialDiffuseBlend);
		continue;
	}
	vec3 lightVec = uni_materialPos[i].w == 1 ? -uni_materialPos[i].xyz : uni_materialPos[i].xyz - worldPosition;
	float distance = length(lightVec);
	float attFactor = uni_materialPos[i].w != 0 ? 1 : uni_materialAtt[i].x + (uni_materialAtt[i].y * distance) + (uni_materialAtt[i].z * distance * distance);
	vec3 ulightVec = normalize(lightVec);
	float ndotl = max(dot(unormal, ulightVec), 0);
	
	float specularFactor = max(dot(reflect(-ulightVec, unormal), ucameraVec), 0.0);
	float dampedFactor = pow(specularFactor, max(1, uni_materialSpecRadius));
	
	totalDiffuse += (ndotl * mix(uni_materialCol[i], uni_materialDiffuseTint, uni_materialDiffuseBlend)) / attFactor;
	totalSpecular += (dampedFactor * uni_materialSpecular * mix(uni_materialCol[i], uni_materialSpecTint, uni_materialSpecBlend)) / attFactor;
}

totalDiffuse *= 1 + uni_materialAmbient;
att_vertMaterialDiffuse = clamp(totalDiffuse * uni_materialDiffuse, uni_materialDiffuseMin, uni_materialDiffuseMax);
att_vertMaterialSpecular = clamp(totalSpecular, uni_materialSpecMin, uni_materialSpecMax);

// END ====================================================================================================================================================================
#vertMaterialF

out_Color *= vec4(att_vertMaterialDiffuse, 1);
out_Color += vec4(att_vertMaterialSpecular * glb_specularMod, 0);

// END ====================================================================================================================================================================
#fragMaterialV

vec3 worldPosition = WORLD_POSITION.xyz;
if(uni_fragMaterialRestrictTangents > 0.5) {
	att_fragMaterialSurfaceNormal = (uni_transform * vec4(in_normal, 0)).xyz;
	att_fragMaterialCameraVec = (inverse(uni_viewMatrix) * vec4(0, 0, 0, 1)).xyz - worldPosition;
	for(int i = 0 ; i < uni_materialCount ; ++i)
		att_fragMaterialLightVec[i] = uni_materialPos[i].w == 1 ? vec4(-uni_materialPos[i].xyz, 1) : vec4(uni_materialPos[i].xyz - worldPosition, uni_materialPos[i].w);
}
else {
	vec3 surfaceNormal = (uni_transform * vec4(in_normal, 0)).xyz;
	vec3 norm = normalize(surfaceNormal), tang = normalize((uni_transform * vec4(in_tangent, 0)).xyz), bitang = normalize(cross(norm, tang));
	
	mat3 tangSpace = mat3(
		tang.x, bitang.x, norm.x,
		tang.y, bitang.y, norm.y,
		tang.z, bitang.z, norm.z
	);
	
	att_fragMaterialCameraVec = tangSpace * ((inverse(uni_viewMatrix) * vec4(0, 0, 0, 1)).xyz - worldPosition);
	for(int i = 0 ; i < uni_materialCount ; ++i)
		att_fragMaterialLightVec[i] = uni_materialPos[i].w == 1 ? vec4(tangSpace * -uni_materialPos[i].xyz, 1) : vec4(tangSpace * (uni_materialPos[i].xyz - worldPosition), uni_materialPos[i].w);
}

// END ====================================================================================================================================================================
#fragMaterialF

vec3 totalDiffuse = vec3(0), totalSpecular = vec3(0);
vec3 unormal = normalize(uni_fragMaterialRestrictTangents > 0.5 ? att_fragMaterialSurfaceNormal : glb_surfaceNormal);
vec3 ucameraVec = normalize(att_fragMaterialCameraVec);

for(int i = 0 ; i < uni_materialCount ; ++i) {
	float type = att_fragMaterialLightVec[i].w;
	vec3 lightVec = att_fragMaterialLightVec[i].xyz;
	if(type > 1.5) {
		totalDiffuse += mix(uni_materialCol[i], uni_materialDiffuseTint, uni_materialDiffuseBlend);
		continue;
	}
	float distance = length(lightVec);
	float attFactor = type > 0 ? 1 : uni_materialAtt[i].x + (uni_materialAtt[i].y * distance) + (uni_materialAtt[i].z * distance * distance);
	vec3 ulightVec = normalize(lightVec);
	float ndotl = max(dot(unormal, ulightVec), 0);
	
	float specularFactor = max(dot(reflect(-ulightVec, unormal), ucameraVec), 0.0);
	float dampedFactor = pow(specularFactor, max(1, uni_materialSpecRadius));
	
	totalDiffuse += (ndotl * mix(uni_materialCol[i], uni_materialDiffuseTint, uni_materialDiffuseBlend)) / attFactor;
	totalSpecular += (dampedFactor * uni_materialSpecular * mix(uni_materialCol[i], uni_materialSpecTint, uni_materialSpecBlend)) / attFactor;
}

totalDiffuse *= 1 + uni_materialAmbient;
out_Color *= vec4(clamp(totalDiffuse * uni_materialDiffuse, uni_materialDiffuseMin, uni_materialDiffuseMax), 1);
out_Color += vec4(clamp(totalSpecular, uni_materialSpecMin, uni_materialSpecMax) * glb_specularMod, 0);
