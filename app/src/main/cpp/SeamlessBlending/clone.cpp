/**
 This file is part of Poisson Image Editing.
 
 Copyright Christoph Heindl 2015
 
 Poisson Image Editing is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Poisson Image Editing is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with Poisson Image Editing.  If not, see <http://www.gnu.org/licenses/>.
 */


#include "clone.h"
#include "poisson_solver.cpp"
#include <opencv2/opencv.hpp>
#pragma warning (push)
#pragma warning (disable: 4244)
#include "../Eigen/Sparse"
#include "../Eigen/Dense"

#pragma warning (pop)

namespace blend {
    
    namespace detail {
        bool findOverlap(cv::InputArray background,
                         cv::InputArray foreground,
                         int offsetX, int offsetY,
                         cv::Rect &rBackground,
                         cv::Rect &rForeground)
        {
            cv::Mat bg = background.getMat();
            cv::Mat fg = foreground.getMat();

            
            rBackground = cv::Rect(0, 0, bg.cols, bg.rows) & 
                          cv::Rect(offsetX, offsetY, fg.cols, fg.rows);


            // Compensate for negative offsets. If offset < 0, offset in foreground is positive.
            rForeground = cv::Rect(std::max<int>(-offsetX, 0), 
                                   std::max<int>(-offsetY, 0), 
                                   rBackground.width, 
                                   rBackground.height);

            
            return rForeground.area() > 0;
            
        }
        
        void computeMixedGradientVectorField(cv::InputArray background,
                                             cv::InputArray foreground,
                                             cv::OutputArray vx_,
                                             cv::OutputArray vy_)
        {
            cv::Mat bg = background.getMat();
            cv::Mat fg = foreground.getMat();
            
            const int channels = bg.channels();
            
            vx_.create(bg.size(), CV_MAKETYPE(CV_32F, channels));
            vy_.create(bg.size(), CV_MAKETYPE(CV_32F, channels));
            
            cv::Mat vx = vx_.getMat();
            cv::Mat vy = vy_.getMat();
            
            cv::Mat kernelx = (cv::Mat_<float>(1, 3) << -0.5, 0, 0.5);
            cv::Mat kernely = (cv::Mat_<float>(3, 1) << -0.5, 0, 0.5);
            
            cv::Mat vxf, vyf, vxb, vyb;
            cv::filter2D(fg, vxf, CV_32F, kernelx, cv::Point(-1,-1), 0, cv::BORDER_REPLICATE);
            cv::filter2D(fg, vyf, CV_32F, kernely, cv::Point(-1,-1), 0, cv::BORDER_REPLICATE);
            cv::filter2D(bg, vxb, CV_32F, kernelx, cv::Point(-1,-1), 0, cv::BORDER_REPLICATE);
            cv::filter2D(bg, vyb, CV_32F, kernely, cv::Point(-1,-1), 0, cv::BORDER_REPLICATE);
            
            
            for(int id = 0; id <= (vx.rows * vx.cols * channels - channels); ++id)
            {
                const cv::Vec2f g[2] = {
                    cv::Vec2f(vxf.ptr<float>()[id], vyf.ptr<float>()[id]),
                    cv::Vec2f(vxb.ptr<float>()[id], vyb.ptr<float>()[id])
                };
                
                int which = (g[0].dot(g[0]) > g[1].dot(g[1])) ? 0 : 1;
                
                vx.ptr<float>()[id] = g[which][0];
                vy.ptr<float>()[id] = g[which][1];
            }
        }
        
        void computeWeightedGradientVectorField(cv::InputArray background,
                                                cv::InputArray foreground,
                                                cv::OutputArray vx,
                                                cv::OutputArray vy,
                                                float weightForeground)
        {
            
            cv::Mat bg = background.getMat();
            cv::Mat fg = foreground.getMat();
            
            cv::Mat kernelx = (cv::Mat_<float>(1, 3) << -0.5, 0, 0.5);
            cv::Mat kernely = (cv::Mat_<float>(3, 1) << -0.5, 0, 0.5);
            
            cv::Mat vxf, vyf, vxb, vyb;
            cv::filter2D(fg, vxf, CV_32F, kernelx, cv::Point(-1,-1), 0, cv::BORDER_REPLICATE);
            cv::filter2D(fg, vyf, CV_32F, kernely, cv::Point(-1,-1), 0, cv::BORDER_REPLICATE);
            cv::filter2D(bg, vxb, CV_32F, kernelx, cv::Point(-1,-1), 0, cv::BORDER_REPLICATE);
            cv::filter2D(bg, vyb, CV_32F, kernely, cv::Point(-1,-1), 0, cv::BORDER_REPLICATE);
            
            cv::addWeighted(vxf, weightForeground, vxb, 1.f - weightForeground, 0, vx);
            cv::addWeighted(vyf, weightForeground, vyb, 1.f - weightForeground, 0, vy);
        }
    }
    
    void seamlessClone(cv::InputArray background,
                       cv::InputArray foreground,
                       cv::InputArray foregroundMask,
                       int offsetX,
                       int offsetY,
                       cv::OutputArray destination,
                       CloneType type)
    {
        
        // Copy original background as we only solve for the overlapping area of the translated foreground mask.
        background.getMat().copyTo(destination);
        
        // Find overlapping region. We will only perform on this region
        cv::Rect rbg, rfg;
        if (!detail::findOverlap(background, foreground, offsetX, offsetY, rbg, rfg))
            return;
        
        // Compute the guidance vector field
        cv::Mat vx, vy;
        switch (type) {
            case CLONE_FOREGROUND_GRADIENTS:
                detail::computeWeightedGradientVectorField(background.getMat()(rbg),
                                                           foreground.getMat()(rfg),
                                                           vx, vy,
                                                           1.f);
                break;
                
            case CLONE_AVERAGED_GRADIENTS:
                detail::computeWeightedGradientVectorField(background.getMat()(rbg),
                                                           foreground.getMat()(rfg),
                                                           vx, vy,
                                                           0.5f);
                break;
                
            case CLONE_MIXED_GRADIENTS:
                detail::computeMixedGradientVectorField(background.getMat()(rbg),
                                                        foreground.getMat()(rfg),
                                                        vx, vy);
                break;
                
            default:
                break;
        }
        
        
        // For the Poisson equation the divergence of the guidance field is necessary.
        cv::Mat vxx, vyy;
        cv::Mat kernelx = (cv::Mat_<float>(1, 3) << -0.5, 0, 0.5);
        cv::Mat kernely = (cv::Mat_<float>(3, 1) << -0.5, 0, 0.5);
        cv::filter2D(vx, vxx, CV_32F, kernelx);
        cv::filter2D(vy, vyy, CV_32F, kernely);
        
        cv::Mat f = vxx + vyy;
                
        cv::Mat boundaryMask(rfg.size(), CV_8UC1);      
        cv::threshold(foregroundMask.getMat()(rfg), boundaryMask, constants::UNKNOWN, constants::DIRICHLET_BD, cv::THRESH_BINARY_INV);
        cv::rectangle(boundaryMask, cv::Rect(0, 0, boundaryMask.cols, boundaryMask.rows), constants::DIRICHLET_BD, 1);

        cv::Mat boundaryValues(rfg.size(), CV_MAKETYPE(CV_32F, background.channels()));
        background.getMat()(rbg).convertTo(boundaryValues, CV_32F);
        
        // Solve Poisson equation
        cv::Mat result;
        solvePoissonEquations(f,
                              boundaryMask,
                              boundaryValues,
                              result);
        
        // Copy result to destination image.
        result.convertTo(destination.getMat()(rbg), CV_8U);
        
    }
    
    
}