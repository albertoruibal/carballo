
x = -1000:1:1000
y = arrayfun(@(x) 1/(1+10^(-x/400)), x)

x1 = load('sigmoid_x1.txt')
y1 = load('sigmoid_y1.txt')

x2 = load('sigmoid_x2.txt')
y2 = load('sigmoid_y2.txt')

plot(x, y, '-', x1, y1, 'o', x2, y2, 'x')
