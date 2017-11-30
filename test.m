%
A00 = imread('image/00.png');
A01 = imread('image/01.png');
A02 = imread('image/02.png');
A03 = imread('image/03.png');
A04 = imread('image/04.png');
A05 = imread('image/05.png');
A06 = imread('image/06.png');
A07 = imread('image/07.png');
A08 = imread('image/08.png');
A09 = imread('image/09.png');
A10 = imread('image/10.png');
A11 = imread('image/11.png');
A12 = imread('image/12.png');
A13 = imread('image/13.png');
A14 = imread('image/14.png');
A15 = imread('image/15.png');
A16 = imread('image/16.png');
A17 = imread('image/17.png');
A18 = imread('image/18.png');
A19 = imread('image/19.png');
A20 = imread('image/20.png');
%}
%{
A = [1 2; 3 4];
B = [10 20; 30 40];
C = zeros(2, 2, 2);
C(1,:,:) = A;
C(2,:,:) = B;
%}
A = zeros(21, 50, 1000);
A(01,:,:) = A00;
A(02,:,:) = A01;
A(03,:,:) = A02;
A(04,:,:) = A03;
A(05,:,:) = A04;
A(06,:,:) = A05;
A(07,:,:) = A06;
A(08,:,:) = A07;
A(09,:,:) = A08;
A(10,:,:) = A09;
A(11,:,:) = A10;
A(12,:,:) = A11;
A(13,:,:) = A12;
A(14,:,:) = A13;
A(15,:,:) = A14;
A(16,:,:) = A15;
A(17,:,:) = A16;
A(18,:,:) = A17;
A(19,:,:) = A18;
A(20,:,:) = A19;
A(21,:,:) = A20;
%{
for k=1:size(A,3)
   a=A(:,:,k);
   [maxv,idx]=max(a(:));
   [ii,jj]=ind2sub([size(A,1) size(A,2)],idx);
   out{k}=[maxv ii jj]
end
celldisp(out)
%}
%{
s = size(A);
[v,ii] = max(reshape(A,[],s(3)));
[i1 j1 ] = ind2sub(s(1:2),ii);
out = [v;i1;j1;1:s(3)]';
%}
