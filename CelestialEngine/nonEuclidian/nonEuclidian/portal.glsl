
#planarPortalF

vec2 texCoords = (att_clipSpacePosition.xy / att_clipSpacePosition.w) / 2.0 + 0.5;
out_Color = texture(uni_planarPortal, texCoords);
