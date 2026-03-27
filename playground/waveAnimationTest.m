indices = 1:1000;
touchIndex = [500]';
radius = 500;
plot(indices, waveAnimationFcn(indices, touchIndex, radius));

function x = waveAnimationFcn(indices, touchIndex, radius)
    t = (indices - touchIndex) / radius;
    x = zeros(length(touchIndex), length(indices));
    for k = 1:length(touchIndex)
        tk = t(k, :);
        xk = x(k, :);
        I = abs(tk) < 1;
        xk(I) = cos(tk(I) * pi / 2) .* exp(-10*tk(I).^2);
        x(k, :) = xk;
    end
end
